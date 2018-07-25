package com.jhl.mds.services.migration.mysql2mysql;

import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;
import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.events.IncrementalStatusUpdateEvent;
import com.jhl.mds.services.mysql.MySQLUpdateService;
import com.jhl.mds.services.mysql.MySQLWriteService;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class IncrementalMigrationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private ApplicationEventPublisher eventPublisher;
    private TaskRepository taskRepository;
    private MySQLBinLogPool mySQLBinLogPool;
    private MySQLBinLogInsertMapperService mySQLBinLogInsertMapperService;
    private MySQLBinLogUpdateMapperService mySQLBinLogUpdateMapperService;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private MySQLWriteService mySQLWriteService;
    private MySQLUpdateService mySQLUpdateService;
    private FullMigrationDTO.Converter fullMigrationDTOConverter;
    private Set<Integer> runningTask = new HashSet<>();
    private Map<Integer, MySQLBinLogListener> listenerMap = new HashMap<>();

    @Autowired
    public IncrementalMigrationService(
            ApplicationEventPublisher eventPublisher,
            TaskRepository taskRepository,
            MySQLBinLogPool mySQLBinLogPool,
            MySQLBinLogInsertMapperService mySQLBinLogInsertMapperService,
            MySQLBinLogUpdateMapperService mySQLBinLogUpdateMapperService,
            MigrationMapperService.Factory migrationMapperServiceFactory,
            MySQLWriteService mySQLWriteService,
            MySQLUpdateService mySQLUpdateService,
            FullMigrationDTO.Converter fullMigrationDTOConverter
    ) {
        this.eventPublisher = eventPublisher;
        this.taskRepository = taskRepository;
        this.mySQLBinLogPool = mySQLBinLogPool;
        this.mySQLBinLogInsertMapperService = mySQLBinLogInsertMapperService;
        this.mySQLBinLogUpdateMapperService = mySQLBinLogUpdateMapperService;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.mySQLWriteService = mySQLWriteService;
        this.mySQLUpdateService = mySQLUpdateService;
        this.fullMigrationDTOConverter = fullMigrationDTOConverter;
    }

    @PostConstruct
    private void init() {
        logger.info("Start incremental migration service");
        List<Task> tasks = taskRepository.findByIncrementalMigrationActive(true);
        for (Task task : tasks) {
            run(fullMigrationDTOConverter.from(task));
        }
    }

    public synchronized void run(FullMigrationDTO dto) {
        logger.info("Run incremental migration for: " + dto);
        if (runningTask.contains(dto.getTaskId())) {
            throw new RuntimeException("Task has already been running");
        }
        runningTask.add(dto.getTaskId());

        eventPublisher.publishEvent(new IncrementalStatusUpdateEvent(dto.getTaskId(), true));

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

    @SuppressWarnings("unchecked")
    private void insert(FullMigrationDTO dto, WriteRowsEventData eventData) {
        try {
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(migrationMapperService.getColumns());

            Pipeline<FullMigrationDTO, Long> pipeline = new Pipeline<>(dto);
            pipeline.append(mySQLBinLogInsertMapperService)
                    .append(migrationMapperService)
                    .append(new PipelineGrouperService<String>(1234))
                    .append(mySQLWriteService)
                    .execute(eventData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // TODO: make sure update run after insert
    private void update(FullMigrationDTO dto, UpdateRowsEventData eventData) {
        try {
            MigrationMapperService migrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(migrationMapperService.getColumns());

            Pipeline<FullMigrationDTO, Long> pipeline = new Pipeline<>(dto);
            pipeline.append(mySQLBinLogUpdateMapperService)
                    .append((PipeLineTaskRunner<FullMigrationDTO, Pair<Map<String, Object>, Map<String, Object>>, Pair<Map<String, Object>, Map<String, Object>>>) (context, input, next, errorHandler) -> {
                        Map<String, Object> key = input.getFirst();
                        Map<String, Object> value = input.getSecond();
                        key = migrationMapperService.map(key, false);
                        value = migrationMapperService.map(value, false);

                        next.accept(Pair.of(key, value));
                    })
                    .append(mySQLUpdateService)
                    .execute(eventData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stop(FullMigrationDTO dto) {
        mySQLBinLogPool.removeListener(dto.getSource(), listenerMap.get(dto.getTaskId()));
        listenerMap.remove(dto.getTaskId());

        runningTask.remove(dto.getTaskId());

        eventPublisher.publishEvent(new IncrementalStatusUpdateEvent(dto.getTaskId(), false));
    }

    public boolean isTaskRunning(int taskId) {
        return runningTask.contains(taskId);
    }
}
