package com.jhl.mds.services.migration.mysql2redis;

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.consts.MigrationAction;
import com.jhl.mds.dto.PairOfMap;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import com.jhl.mds.services.customefilter.CustomFilterService;
import com.jhl.mds.services.migration.InsertOrDeleteWhenUpdatingService;
import com.jhl.mds.services.migration.mysql2mysql.MigrationMapperService;
import com.jhl.mds.services.mysql.MySQLEventPrimaryKeyLock;
import com.jhl.mds.services.mysql.binlog.*;
import com.jhl.mds.services.redis.RedisDeleteService;
import com.jhl.mds.services.redis.RedisInsertService;
import com.jhl.mds.services.redis.RedisUpdateService;
import com.jhl.mds.services.task.TaskStatisticService;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import com.jhl.mds.util.pipeline.Pipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service(("mysql2RedisIncrementalMigrationService"))
@Slf4j
public class IncrementalMigrationService {

    private Map<Integer, ExecutorService> executorServiceMap = new HashMap<>();
    private Set<Integer> runningTask = new HashSet<>();
    private MySQLBinLogPool mySQLBinLogPool;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private MySQLBinLogInsertMapperService mySQLBinLogInsertMapperService;
    private MySQLBinLogUpdateMapperService mySQLBinLogUpdateMapperService;
    private MySQLBinLogDeleteMapperService mySQLBinLogDeleteMapperService;
    private MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock;
    private InsertOrDeleteWhenUpdatingService insertOrDeleteWhenUpdatingService;
    private CustomFilterService customFilterService;
    private RedisInsertService redisInsertService;
    private RedisUpdateService redisUpdateService;
    private RedisDeleteService redisDeleteService;
    private TaskStatisticService taskStatisticService;
    private Map<Integer, MySQLBinLogListener> listenerMap = new HashMap<>();

    public IncrementalMigrationService(
            MySQLBinLogPool mySQLBinLogPool,
            MigrationMapperService.Factory migrationMapperServiceFactory,
            MySQLBinLogInsertMapperService mySQLBinLogInsertMapperService,
            MySQLBinLogUpdateMapperService mySQLBinLogUpdateMapperService,
            MySQLBinLogDeleteMapperService mySQLBinLogDeleteMapperService,
            MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock,
            InsertOrDeleteWhenUpdatingService insertOrDeleteWhenUpdatingService,
            CustomFilterService customFilterService,
            RedisInsertService redisInsertService,
            RedisUpdateService redisUpdateService,
            RedisDeleteService redisDeleteService,
            TaskStatisticService taskStatisticService
    ) {
        this.mySQLBinLogPool = mySQLBinLogPool;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.mySQLBinLogInsertMapperService = mySQLBinLogInsertMapperService;
        this.mySQLBinLogUpdateMapperService = mySQLBinLogUpdateMapperService;
        this.mySQLBinLogDeleteMapperService = mySQLBinLogDeleteMapperService;
        this.mySQLEventPrimaryKeyLock = mySQLEventPrimaryKeyLock;
        this.insertOrDeleteWhenUpdatingService = insertOrDeleteWhenUpdatingService;
        this.customFilterService = customFilterService;
        this.redisInsertService = redisInsertService;
        this.redisUpdateService = redisUpdateService;
        this.redisDeleteService = redisDeleteService;
        this.taskStatisticService = taskStatisticService;
    }

    public synchronized void run(MySQL2RedisMigrationDTO dto) {
        log.info("Run incremental migration for: " + dto);
        if (runningTask.contains(dto.getTaskId())) {
            throw new RuntimeException("Task has already been running");
        }
        runningTask.add(dto.getTaskId());

        ExecutorService executor = getExecutorServiceForTaskId(dto.getTaskId(), dto.isSequential());

        MySQLBinLogListener listener = new MySQLBinLogListener() {
            @Override
            public void insert(WriteRowsEventData eventData) {
                if (MigrationAction.INSERT.isApplicable(dto.getMigrationActionCode())) {
                    executor.submit(() -> IncrementalMigrationService.this.insert(dto, eventData));
                }
            }

            @Override
            public void update(UpdateRowsEventData eventData) {
                if (MigrationAction.UPDATE.isApplicable(dto.getMigrationActionCode())) {
                    executor.submit(() -> IncrementalMigrationService.this.update(dto, eventData));
                }
            }

            @Override
            public void delete(DeleteRowsEventData eventData) {
                if (MigrationAction.DELETE.isApplicable(dto.getMigrationActionCode())) {
                    executor.submit(() -> IncrementalMigrationService.this.delete(dto, eventData));
                }
            }
        };

        listenerMap.put(dto.getTaskId(), listener);

        mySQLBinLogPool.addListener(dto.getSource(), listener);
    }

    private void insert(MySQL2RedisMigrationDTO dto, WriteRowsEventData eventData) {
        try {
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getMapping());

            Set<Object> tmpInsertingPrimaryKeys = new HashSet<>();

            Pipeline<MySQL2RedisMigrationDTO, WriteRowsEventData, WriteRowsEventData> pipeline = Pipeline.of(dto, WriteRowsEventData.class);
            pipeline.append(mySQLBinLogInsertMapperService)
                    .append((PipeLineTaskRunner<MySQL2RedisMigrationDTO, Map<String, Object>, Map<String, Object>>) (context, input, next, errorHandler) -> {
                        tmpInsertingPrimaryKeys.add(mySQLEventPrimaryKeyLock.lock(context, input));
                        next.accept(input);
                    })
                    .append(customFilterService)
                    .append(migrationMapperService)
                    .append(redisInsertService)
                    .append((context, input, next, errorHandler) -> taskStatisticService.updateTaskIncrementalStatistic(dto.getTaskId(), 1, 0, 0))
                    .execute(eventData)
                    .waitForFinish();

            mySQLEventPrimaryKeyLock.unlock(dto, tmpInsertingPrimaryKeys);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void update(MySQL2RedisMigrationDTO dto, UpdateRowsEventData eventData) {
        MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getMapping());

        Pipeline<MySQL2RedisMigrationDTO, UpdateRowsEventData, UpdateRowsEventData> pipeline = Pipeline.of(dto, UpdateRowsEventData.class);

        Set<Object> tmpInsertingPrimaryKeys = new HashSet<>();

        try {
            pipeline.append(mySQLBinLogUpdateMapperService)
                    .append((PipeLineTaskRunner<MySQL2RedisMigrationDTO, PairOfMap, PairOfMap>) (context, input, next, errorHandler) -> {
                        tmpInsertingPrimaryKeys.add(mySQLEventPrimaryKeyLock.lock(context, input.getFirst()));
                        next.accept(input);
                    })
                    .append(insertOrDeleteWhenUpdatingService)
                    .append((PipeLineTaskRunner<MySQL2RedisMigrationDTO, PairOfMap, PairOfMap>) (context, input, next, errorHandler) -> {
                        Map<String, Object> key = input.getFirst();
                        Map<String, Object> value = input.getSecond();
                        key = migrationMapperService.map(key, false);
                        value = migrationMapperService.map(value, false);

                        PairOfMap pair = PairOfMap.of(key, value);
                        pair.setDeleteNeeded(input.isDeleteNeeded());
                        pair.setInsertNeeded(input.isInsertNeeded());
                        next.accept(pair);
                    })
                    .append((PipeLineTaskRunner<MySQL2RedisMigrationDTO, PairOfMap, PairOfMap>) (context, input, next, errorHandler) -> {
                        if (input.isDeleteNeeded())
                            redisDeleteService.execute(dto, input.getFirst(), o -> taskStatisticService.updateTaskIncrementalStatistic(dto.getTaskId(), 0, 0, 1), errorHandler);
                        else next.accept(input);
                    })
                    .append((PipeLineTaskRunner<MySQL2RedisMigrationDTO, PairOfMap, PairOfMap>) (context, input, next, errorHandler) -> {
                        if (input.isInsertNeeded())
                            redisInsertService.execute(dto, input.getSecond(), o -> taskStatisticService.updateTaskIncrementalStatistic(dto.getTaskId(), 1, 0, 0), errorHandler);
                        else next.accept(input);
                    })
                    .append(redisUpdateService)
                    .append((context, input, next, errorHandler) -> taskStatisticService.updateTaskIncrementalStatistic(dto.getTaskId(), 0, 1, 0))
                    .execute(eventData)
                    .waitForFinish();

            mySQLEventPrimaryKeyLock.unlock(dto, tmpInsertingPrimaryKeys);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void delete(MySQL2RedisMigrationDTO dto, DeleteRowsEventData eventData) {
        try {
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getMapping());

            Set<Object> tmpInsertingPrimaryKeys = new HashSet<>();

            Pipeline<MySQL2RedisMigrationDTO, DeleteRowsEventData, DeleteRowsEventData> pipeline = Pipeline.of(dto, DeleteRowsEventData.class);
            pipeline.append(mySQLBinLogDeleteMapperService)
                    .append(customFilterService)
                    .append((PipeLineTaskRunner<MySQL2RedisMigrationDTO, Map<String, Object>, Map<String, Object>>) (context, input, next, errorHandler) -> {
                        tmpInsertingPrimaryKeys.add(mySQLEventPrimaryKeyLock.lock(context, input));
                        next.accept(input);
                    })
                    .append(migrationMapperService)
                    .append(redisDeleteService)
                    .append((context, input, next, errorHandler) -> taskStatisticService.updateTaskIncrementalStatistic(dto.getTaskId(), 0, 0, 1))
                    .execute(eventData)
                    .waitForFinish();

            mySQLEventPrimaryKeyLock.unlock(dto, tmpInsertingPrimaryKeys);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized ExecutorService getExecutorServiceForTaskId(int taskId, boolean sequential) {
        if (!executorServiceMap.containsKey(taskId))
            executorServiceMap.put(taskId, Executors.newFixedThreadPool(sequential ? 1 : 4));
        return executorServiceMap.get(taskId);
    }

    public synchronized void stop(MySQL2RedisMigrationDTO dto) {
        mySQLBinLogPool.removeListener(dto.getSource(), listenerMap.get(dto.getTaskId()));
        listenerMap.remove(dto.getTaskId());

        runningTask.remove(dto.getTaskId());

        executorServiceMap.get(dto.getTaskId()).shutdownNow();
        executorServiceMap.remove(dto.getTaskId());
    }
}
