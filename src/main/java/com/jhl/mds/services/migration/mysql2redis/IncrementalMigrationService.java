package com.jhl.mds.services.migration.mysql2redis;

import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dto.migration.MySQL2RedisMigrationDTO;
import com.jhl.mds.services.migration.mysql2mysql.MigrationMapperService;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogInsertMapperService;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogListener;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogPool;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogUpdateMapperService;
import com.jhl.mds.services.redis.RedisInsertService;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import com.jhl.mds.util.pipeline.Pipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
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
    private RedisInsertService redisInsertService;

    public IncrementalMigrationService(
            MySQLBinLogPool mySQLBinLogPool,
            MigrationMapperService.Factory migrationMapperServiceFactory,
            MySQLBinLogInsertMapperService mySQLBinLogInsertMapperService,
            MySQLBinLogUpdateMapperService mySQLBinLogUpdateMapperService,
            RedisInsertService redisInsertService
    ) {
        this.mySQLBinLogPool = mySQLBinLogPool;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.mySQLBinLogInsertMapperService = mySQLBinLogInsertMapperService;
        this.mySQLBinLogUpdateMapperService = mySQLBinLogUpdateMapperService;
        this.redisInsertService = redisInsertService;
    }

    public synchronized void run(MySQL2RedisMigrationDTO dto) {
        log.info("Run incremental migration for: " + dto);
        if (runningTask.contains(dto.getTaskId())) {
            throw new RuntimeException("Task has already been running");
        }
        runningTask.add(dto.getTaskId());

        ExecutorService executor = getExecutorServiceForTaskId(dto.getTaskId());

        MySQLBinLogListener listener = new MySQLBinLogListener() {
            @Override
            public void insert(WriteRowsEventData eventData) {
                executor.submit(() -> IncrementalMigrationService.this.insert(dto, eventData));
            }

            @Override
            public void update(UpdateRowsEventData eventData) {
                executor.submit(() -> IncrementalMigrationService.this.update(dto, eventData));
            }
        };

        mySQLBinLogPool.addListener(dto.getSource(), listener);
    }

    @SuppressWarnings("unchecked")
    private void insert(MySQL2RedisMigrationDTO dto, WriteRowsEventData eventData) {
        try {
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getMapping());

            Pipeline<MySQL2RedisMigrationDTO, WriteRowsEventData, WriteRowsEventData> pipeline = Pipeline.of(dto, WriteRowsEventData.class);
            pipeline.append(mySQLBinLogInsertMapperService)
                    .append(migrationMapperService)
                    .append(redisInsertService)
                    .execute(eventData)
                    .waitForFinish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update(MySQL2RedisMigrationDTO dto, UpdateRowsEventData eventData) {
        MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getMapping());

        Pipeline<MySQL2RedisMigrationDTO, UpdateRowsEventData, UpdateRowsEventData> pipeline = Pipeline.of(dto, UpdateRowsEventData.class);
        try {
            pipeline.append(mySQLBinLogUpdateMapperService)
                    .append((PipeLineTaskRunner<MySQL2RedisMigrationDTO, Pair<Map<String, Object>, Map<String, Object>>, Map<String, Object>>) (context, input, next, errorHandler) -> {
                        Map<String, Object> key = input.getFirst();
                        Map<String, Object> value = input.getSecond();
                        key = migrationMapperService.map(key, false);
                        value = migrationMapperService.map(value, false);

//                        next.accept(Pair.of(key, value));
                        next.accept(value);
                    })
                    .append(redisInsertService)
                    .execute(eventData)
                    .waitForFinish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized ExecutorService getExecutorServiceForTaskId(int taskId) {
        if (!executorServiceMap.containsKey(taskId))
            executorServiceMap.put(taskId, Executors.newFixedThreadPool(4));
        return executorServiceMap.get(taskId);
    }
}
