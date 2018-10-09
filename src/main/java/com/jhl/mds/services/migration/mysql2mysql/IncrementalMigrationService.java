package com.jhl.mds.services.migration.mysql2mysql;

import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.consts.MySQLConstants;
import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskStatistics;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dao.repositories.TaskStatisticsRepository;
import com.jhl.mds.dto.IncrementalMigrationProgressDTO;
import com.jhl.mds.dto.MigrationDTO;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.events.IncrementalStatusUpdateEvent;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import com.jhl.mds.services.mysql.MySQLInsertService;
import com.jhl.mds.services.mysql.MySQLPrimaryKeyService;
import com.jhl.mds.services.mysql.MySQLUpdateService;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogInsertMapperService;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogListener;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogPool;
import com.jhl.mds.services.mysql.binlog.MySQLBinLogUpdateMapperService;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import com.jhl.mds.util.pipeline.Pipeline;
import com.jhl.mds.util.pipeline.PipelineGrouperService;
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
public class IncrementalMigrationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<Integer, ExecutorService> executorServiceMap = new HashMap<>();
    @Value("${mds.incremental.autostart:true}")
    private boolean enableAutoStart;
    private ApplicationEventPublisher eventPublisher;
    private TaskRepository taskRepository;
    private TaskStatisticsRepository taskStatisticsRepository;
    private MySQLBinLogPool mySQLBinLogPool;
    private MySQLBinLogInsertMapperService mySQLBinLogInsertMapperService;
    private MySQLBinLogUpdateMapperService mySQLBinLogUpdateMapperService;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private MySQLPrimaryKeyService mySQLPrimaryKeyService;
    private MySQLInsertService mySQLInsertService;
    private MySQLUpdateService mySQLUpdateService;
    private MySQLDescribeService mySQLDescribeService;
    private MigrationDTO.Converter fullMigrationDTOConverter;
    private Set<Integer> runningTask = new HashSet<>();
    private Map<Integer, MySQLBinLogListener> listenerMap = new HashMap<>();
    private final Map<Integer, Set> insertingPrimaryKeyMap = new HashMap<>();

    @Autowired
    public IncrementalMigrationService(
            ApplicationEventPublisher eventPublisher,
            TaskRepository taskRepository,
            TaskStatisticsRepository taskStatisticsRepository,
            MySQLBinLogPool mySQLBinLogPool,
            MySQLBinLogInsertMapperService mySQLBinLogInsertMapperService,
            MySQLBinLogUpdateMapperService mySQLBinLogUpdateMapperService,
            MigrationMapperService.Factory migrationMapperServiceFactory,
            MySQLPrimaryKeyService mySQLPrimaryKeyService,
            MySQLInsertService mySQLInsertService,
            MySQLUpdateService mySQLUpdateService,
            MySQLDescribeService mySQLDescribeService,
            MigrationDTO.Converter fullMigrationDTOConverter
    ) {
        this.eventPublisher = eventPublisher;
        this.taskRepository = taskRepository;
        this.taskStatisticsRepository = taskStatisticsRepository;
        this.mySQLBinLogPool = mySQLBinLogPool;
        this.mySQLBinLogInsertMapperService = mySQLBinLogInsertMapperService;
        this.mySQLBinLogUpdateMapperService = mySQLBinLogUpdateMapperService;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.mySQLPrimaryKeyService = mySQLPrimaryKeyService;
        this.mySQLInsertService = mySQLInsertService;
        this.mySQLUpdateService = mySQLUpdateService;
        this.mySQLDescribeService = mySQLDescribeService;
        this.fullMigrationDTOConverter = fullMigrationDTOConverter;
    }

    @PostConstruct
    private void init() {
        if (enableAutoStart) {
            logger.info("Start incremental migration service");
            List<Task> tasks = taskRepository.findByIncrementalMigrationActive(true);
            for (Task task : tasks) {
                run(fullMigrationDTOConverter.from(task));
            }
        }
    }

    public synchronized void run(MigrationDTO dto) {
        logger.info("Run incremental migration for: " + dto);
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
        };

        listenerMap.put(dto.getTaskId(), listener);

        mySQLBinLogPool.addListener(dto.getSource(), listener);
    }

    private synchronized ExecutorService getExecutorServiceForTaskId(int taskId) {
        if (!executorServiceMap.containsKey(taskId))
            executorServiceMap.put(taskId, Executors.newFixedThreadPool(4));
        return executorServiceMap.get(taskId);
    }

    @SuppressWarnings("unchecked")
    private void insert(MigrationDTO dto, WriteRowsEventData eventData) {
        try {
            List<MySQLFieldDTO> sourceFields = mySQLDescribeService.getFields(dto.getSource());

            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(migrationMapperService.getColumns());

            Set tmpInsertingPrimaryKeys = new HashSet<>();

            Set insertingPrimaryKeys = getInsertingPrimaryKeysForTask(dto.getTaskId());

            Pipeline<MigrationDTO, WriteRowsEventData, WriteRowsEventData> pipeline = Pipeline.of(dto, WriteRowsEventData.class);
            pipeline.append(mySQLBinLogInsertMapperService)
                    .append((PipeLineTaskRunner<MigrationDTO, Map<String, Object>, Map<String, Object>>) (context, input, next, errorHandler) -> {
                        synchronized (insertingPrimaryKeys) {
                            Object primaryKeyValue = mySQLPrimaryKeyService.getPrimaryKeyValue(input, sourceFields);
                            insertingPrimaryKeys.add(primaryKeyValue);
                            tmpInsertingPrimaryKeys.add(primaryKeyValue);
                        }
                        next.accept(input);
                    })
                    .append(migrationMapperService)
                    .append(new PipelineGrouperService<>(MySQLConstants.MYSQL_INSERT_CHUNK_SIZE))
                    .append(mySQLInsertService)
                    .append((context, input, next, errorHandler) -> {
                        updateStatistics(dto, 1, 0, 0);
                    })
                    .execute(eventData)
                    .waitForFinish();

            synchronized (insertingPrimaryKeys) {
                insertingPrimaryKeys.removeAll(tmpInsertingPrimaryKeys);
                insertingPrimaryKeys.notifyAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized Set getInsertingPrimaryKeysForTask(int taskId) {
        if (!insertingPrimaryKeyMap.containsKey(taskId)) insertingPrimaryKeyMap.put(taskId, new HashSet());
        return insertingPrimaryKeyMap.get(taskId);
    }

    // TODO: make sure update run after insert
    private void update(MigrationDTO dto, UpdateRowsEventData eventData) {
        try {
            List<MySQLFieldDTO> sourceFields = mySQLDescribeService.getFields(dto.getSource());

            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(migrationMapperService.getColumns());

            Set insertingPrimaryKeys = getInsertingPrimaryKeysForTask(dto.getTaskId());

            Pipeline<MigrationDTO, UpdateRowsEventData, UpdateRowsEventData> pipeline = Pipeline.of(dto, UpdateRowsEventData.class);
            pipeline.append(mySQLBinLogUpdateMapperService)
                    .append((PipeLineTaskRunner<MigrationDTO, Pair<Map<String, Object>, Map<String, Object>>, Pair<Map<String, Object>, Map<String, Object>>>) (context, input, next, errorHandler) -> {
                        Object primaryKeyValue = mySQLPrimaryKeyService.getPrimaryKeyValue(input.getFirst(), sourceFields);
                        synchronized (insertingPrimaryKeys) {
                            while (insertingPrimaryKeys.contains(primaryKeyValue)) {
                                insertingPrimaryKeys.wait();
                            }
                        }
                        next.accept(input);
                    })
                    .append((PipeLineTaskRunner<MigrationDTO, Pair<Map<String, Object>, Map<String, Object>>, Pair<Map<String, Object>, Map<String, Object>>>) (context, input, next, errorHandler) -> {
                        Map<String, Object> key = input.getFirst();
                        Map<String, Object> value = input.getSecond();
                        key = migrationMapperService.map(key, false);
                        value = migrationMapperService.map(value, false);

                        next.accept(Pair.of(key, value));
                    })
                    .append(mySQLUpdateService)
                    .append((context, input, next, errorHandler) -> {
                        updateStatistics(dto, 0, 1, 0);
                    })
                    .execute(eventData)
                    .waitForFinish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: fix synchronization
    private void updateStatistics(MigrationDTO dto, long insertDelta, long updateDelta, long deleteDelta) {
        synchronized (IncrementalMigrationService.this) {
            taskStatisticsRepository.updateStatistics(dto.getTaskId(), insertDelta, updateDelta, deleteDelta, new Date());
        }

        eventPublisher.publishEvent(new IncrementalStatusUpdateEvent(dto.getTaskId(), true, insertDelta, updateDelta, 0L, true));
    }

    public synchronized void stop(MigrationDTO dto) {
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
