package com.jhl.mds.services.migration.mysql2mysql;

import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.consts.MySQLConstants;
import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskStatistics;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dao.repositories.TaskStatisticsRepository;
import com.jhl.mds.dto.IncrementalMigrationProgressDTO;
import com.jhl.mds.dto.PairOfMap;
import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.events.IncrementalStatusUpdateEvent;
import com.jhl.mds.services.mysql.*;
import com.jhl.mds.services.mysql.binlog.*;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import com.jhl.mds.util.pipeline.Pipeline;
import com.jhl.mds.util.pipeline.PipelineGrouperService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service("mysql2MysqlIncrementalMigrationService")
@Slf4j
public class IncrementalMigrationService {

    private Map<Integer, ExecutorService> executorServiceMap = new HashMap<>();
    @Value("${mds.incremental.autostart:true}")
    private boolean enableAutoStart;
    private ApplicationEventPublisher eventPublisher;
    private TaskRepository taskRepository;
    private TaskStatisticsRepository taskStatisticsRepository;
    private MySQLBinLogPool mySQLBinLogPool;
    private MySQLBinLogInsertMapperService mySQLBinLogInsertMapperService;
    private MySQLBinLogUpdateMapperService mySQLBinLogUpdateMapperService;
    private MySQLBinLogDeleteMapperService mySQLBinLogDeleteMapperService;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private MapToStringService mapToStringService;
    private MySQLInsertService mySQLInsertService;
    private MySQLUpdateService mySQLUpdateService;
    private MySQLDeleteService mySQLDeleteService;
    private MySQL2MySQLMigrationDTO.Converter fullMigrationDTOConverter;
    private MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock;
    private Set<Integer> runningTask = new HashSet<>();
    private Map<Integer, MySQLBinLogListener> listenerMap = new HashMap<>();

    @Autowired
    public IncrementalMigrationService(
            ApplicationEventPublisher eventPublisher,
            TaskRepository taskRepository,
            TaskStatisticsRepository taskStatisticsRepository,
            MySQLBinLogPool mySQLBinLogPool,
            MySQLBinLogInsertMapperService mySQLBinLogInsertMapperService,
            MySQLBinLogUpdateMapperService mySQLBinLogUpdateMapperService,
            MySQLBinLogDeleteMapperService mySQLBinLogDeleteMapperService,
            MigrationMapperService.Factory migrationMapperServiceFactory,
            MapToStringService mapToStringService,
            MySQLInsertService mySQLInsertService,
            MySQLUpdateService mySQLUpdateService,
            MySQLDeleteService mySQLDeleteService,
            MySQL2MySQLMigrationDTO.Converter fullMigrationDTOConverter,
            MySQLEventPrimaryKeyLock mySQLEventPrimaryKeyLock
    ) {
        this.eventPublisher = eventPublisher;
        this.taskRepository = taskRepository;
        this.taskStatisticsRepository = taskStatisticsRepository;
        this.mySQLBinLogPool = mySQLBinLogPool;
        this.mySQLBinLogInsertMapperService = mySQLBinLogInsertMapperService;
        this.mySQLBinLogUpdateMapperService = mySQLBinLogUpdateMapperService;
        this.mySQLBinLogDeleteMapperService = mySQLBinLogDeleteMapperService;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.mapToStringService = mapToStringService;
        this.mySQLInsertService = mySQLInsertService;
        this.mySQLUpdateService = mySQLUpdateService;
        this.mySQLDeleteService = mySQLDeleteService;
        this.fullMigrationDTOConverter = fullMigrationDTOConverter;
        this.mySQLEventPrimaryKeyLock = mySQLEventPrimaryKeyLock;
    }

    @PostConstruct
    private void init() {
        if (enableAutoStart) {
            log.info("Start incremental migration service");
            List<Task> tasks = taskRepository.findByIncrementalMigrationActive(true);
            for (Task task : tasks) {
                run(fullMigrationDTOConverter.from(task));
            }
        }
    }

    public synchronized void run(MySQL2MySQLMigrationDTO dto) {
        log.info("Run incremental migration for: " + dto);
        if (runningTask.contains(dto.getTaskId())) {
            throw new RuntimeException("Task has already been running");
        }
        runningTask.add(dto.getTaskId());

        ExecutorService executor = getExecutorServiceForTaskId(dto.getTaskId());

        eventPublisher.publishEvent(new IncrementalStatusUpdateEvent(dto.getTaskId(), true, null, null, null, false));

        MySQLBinLogListener listener = new MySQLBinLogListener() {
            @Override
            public void insert(WriteRowsEventData eventData) {
                executor.submit(() -> IncrementalMigrationService.this.insert(dto, eventData));
            }

            @Override
            public void update(UpdateRowsEventData eventData) {
                executor.submit(() -> IncrementalMigrationService.this.update(dto, eventData));
            }

            @Override
            public void delete(DeleteRowsEventData eventData) {
                executor.submit(() -> IncrementalMigrationService.this.delete(dto, eventData));
            }
        };

        listenerMap.put(dto.getTaskId(), listener);

        mySQLBinLogPool.addListener(dto.getSource(), listener);
    }

    private synchronized ExecutorService getExecutorServiceForTaskId(int taskId) {
        if (!executorServiceMap.containsKey(taskId))
            executorServiceMap.put(taskId, Executors.newFixedThreadPool(4));
        return executorServiceMap.get(taskId);
    }

    private void insert(MySQL2MySQLMigrationDTO dto, WriteRowsEventData eventData) {
        try {
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(migrationMapperService.getColumns());

            Set<Object> tmpInsertingPrimaryKeys = new HashSet<>();

            Pipeline<MySQL2MySQLMigrationDTO, WriteRowsEventData, WriteRowsEventData> pipeline = Pipeline.of(dto, WriteRowsEventData.class);
            pipeline.append(mySQLBinLogInsertMapperService)
                    .append((PipeLineTaskRunner<MySQL2MySQLMigrationDTO, Map<String, Object>, Map<String, Object>>) (context, input, next, errorHandler) -> {
                        tmpInsertingPrimaryKeys.add(mySQLEventPrimaryKeyLock.lock(context, input));
                        next.accept(input);
                    })
                    .append(migrationMapperService)
                    .append(mapToStringService)
                    .append(new PipelineGrouperService<>(MySQLConstants.MYSQL_INSERT_CHUNK_SIZE))
                    .append(mySQLInsertService)
                    .append((context, input, next, errorHandler) -> updateStatistics(dto, 1, 0, 0))
                    .execute(eventData)
                    .waitForFinish();

            mySQLEventPrimaryKeyLock.unlock(dto, tmpInsertingPrimaryKeys);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void update(MySQL2MySQLMigrationDTO dto, UpdateRowsEventData eventData) {
        try {
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(migrationMapperService.getColumns());

            Set<Object> tmpInsertingPrimaryKeys = new HashSet<>();

            Pipeline<MySQL2MySQLMigrationDTO, UpdateRowsEventData, UpdateRowsEventData> pipeline = Pipeline.of(dto, UpdateRowsEventData.class);
            pipeline.append(mySQLBinLogUpdateMapperService)
                    .append((PipeLineTaskRunner<MySQL2MySQLMigrationDTO, PairOfMap, PairOfMap>) (context, input, next, errorHandler) -> {
                        tmpInsertingPrimaryKeys.add(mySQLEventPrimaryKeyLock.lock(context, input.getFirst()));
                        next.accept(input);
                    })
                    .append((PipeLineTaskRunner<MySQL2MySQLMigrationDTO, PairOfMap, PairOfMap>) (context, input, next, errorHandler) -> {
                        Map<String, Object> key = input.getFirst();
                        Map<String, Object> value = input.getSecond();
                        key = migrationMapperService.map(key, false);
                        value = migrationMapperService.map(value, false);

                        next.accept(PairOfMap.of(key, value));
                    })
                    .append(mySQLUpdateService)
                    .append((context, input, next, errorHandler) -> updateStatistics(dto, 0, 1, 0))
                    .execute(eventData)
                    .waitForFinish();

            mySQLEventPrimaryKeyLock.unlock(dto, tmpInsertingPrimaryKeys);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delete(MySQL2MySQLMigrationDTO dto, DeleteRowsEventData eventData) {
        try {
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(migrationMapperService.getColumns());

            Set<Object> tmpInsertingPrimaryKeys = new HashSet<>();

            Pipeline<MySQL2MySQLMigrationDTO, DeleteRowsEventData, DeleteRowsEventData> pipeline = Pipeline.of(dto, DeleteRowsEventData.class);
            pipeline.append(mySQLBinLogDeleteMapperService)
                    .append((PipeLineTaskRunner<MySQL2MySQLMigrationDTO, Map<String, Object>, Map<String, Object>>) (context, input, next, errorHandler) -> {
                        tmpInsertingPrimaryKeys.add(mySQLEventPrimaryKeyLock.lock(context, input));
                        next.accept(input);
                    })
                    .append(migrationMapperService)
                    .append(mySQLDeleteService)
                    .append((context, input, next, errorHandler) -> updateStatistics(dto, 0, 0, 1))
                    .execute(eventData)
                    .waitForFinish();

            mySQLEventPrimaryKeyLock.unlock(dto, tmpInsertingPrimaryKeys);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: fix synchronization
    private void updateStatistics(MySQL2MySQLMigrationDTO dto, long insertDelta, long updateDelta, long deleteDelta) {
        synchronized (IncrementalMigrationService.this) {
            taskStatisticsRepository.updateStatistics(dto.getTaskId(), insertDelta, updateDelta, deleteDelta, new Date());
        }

        eventPublisher.publishEvent(new IncrementalStatusUpdateEvent(dto.getTaskId(), true, insertDelta, updateDelta, 0L, true));
    }

    public synchronized void stop(MySQL2MySQLMigrationDTO dto) {
        mySQLBinLogPool.removeListener(dto.getSource(), listenerMap.get(dto.getTaskId()));
        listenerMap.remove(dto.getTaskId());

        runningTask.remove(dto.getTaskId());

        eventPublisher.publishEvent(new IncrementalStatusUpdateEvent(dto.getTaskId(), false, null, null, null, false));
    }

    public boolean isTaskRunning(int taskId) {
        return runningTask.contains(taskId);
    }

    public IncrementalMigrationProgressDTO getIncrementalMigrationProgress(int taskId) {
        try {
            TaskStatistics taskStatistics = taskStatisticsRepository.findByFkTaskId(taskId);
            return new IncrementalMigrationProgressDTO(isTaskRunning(taskId), taskStatistics.getInsertCount(), taskStatistics.getUpdateCount(), taskStatistics.getDeleteCount(), false);
        } catch (Exception e) {
            return new IncrementalMigrationProgressDTO(isTaskRunning(taskId), 0L, 0L, 0L, false);
        }
    }
}
