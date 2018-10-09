package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.consts.MySQLConstants;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;
import com.jhl.mds.events.FullMigrationProgressUpdateEvent;
import com.jhl.mds.services.customefilter.CustomFilterService;
import com.jhl.mds.services.mysql.MySQLInsertService;
import com.jhl.mds.services.mysql.MySQLReadService;
import com.jhl.mds.util.pipeline.Pipeline;
import com.jhl.mds.util.pipeline.PipelineGrouperService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Service("mysql2mysqlFullMigrationService")
public class FullMigrationService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private ApplicationEventPublisher eventPublisher;
    private MySQLReadService mySQLReadService;
    private CustomFilterService customFilterService;
    private MapToStringService mapToStringService;
    private MySQLInsertService mySQLInsertService;
    private TaskRepository taskRepository;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private Set<Integer> runningTask = new HashSet<>();

    public FullMigrationService(
            ApplicationEventPublisher eventPublisher,
            MySQLReadService mySQLReadService,
            CustomFilterService customFilterService,
            MapToStringService mapToStringService,
            MySQLInsertService mySQLInsertService,
            TaskRepository taskRepository,
            MigrationMapperService.Factory migrationMapperServiceFactory
    ) {
        this.eventPublisher = eventPublisher;
        this.mySQLReadService = mySQLReadService;
        this.customFilterService = customFilterService;
        this.mapToStringService = mapToStringService;
        this.mySQLInsertService = mySQLInsertService;
        this.taskRepository = taskRepository;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
    }

    public void queue(MySQL2MySQLMigrationDTO dto) {
        if (runningTask.contains(dto.getTaskId())) {
            throw new RuntimeException("Task has already been running");
        }
        runningTask.add(dto.getTaskId());
        saveFullMigrationProgress(dto, 0, false);
        new Thread(() -> run(dto)).start();
    }

    public void run(MySQL2MySQLMigrationDTO dto) {
        try {
            MigrationMapperService mapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(mapperService.getColumns());

            long count = mySQLReadService.count(dto.getSource());
            AtomicLong lastProgress = new AtomicLong();
            AtomicLong finished = new AtomicLong();

            final Consumer<Long> finishCallback = size -> {
                synchronized (lastProgress) {
                    long progress = finished.addAndGet(size) * 100 / count;
                    if (progress > lastProgress.get()) {
                        saveFullMigrationProgress(dto, progress, true);
                    }
                    lastProgress.set(progress);
                }
            };

            Pipeline<MySQL2MySQLMigrationDTO, Object, Object> pipeline = Pipeline.of(dto, Object.class);
            pipeline.setErrorHandler(e -> {
                if (e instanceof MySQLInsertService.WriteServiceException) {
                    finishCallback.accept(((MySQLInsertService.WriteServiceException) e).getCount());
                } else {
                    finishCallback.accept(1L);
                }
            });

            pipeline.append(mySQLReadService)
                    .append(customFilterService)
                    .append(mapperService)
                    .append(mapToStringService)
                    .append(new PipelineGrouperService<>(MySQLConstants.MYSQL_INSERT_CHUNK_SIZE))
                    .append(mySQLInsertService)
                    .append((context, input, next, errorHandler) -> finishCallback.accept(input))
                    .execute()
                    .waitForFinish();

        } catch (Exception e) {
            e.printStackTrace();
        }


        runningTask.remove(dto.getTaskId());
    }

    private void saveFullMigrationProgress(MySQL2MySQLMigrationDTO dto, double progress, boolean async) {
        eventPublisher.publishEvent(new FullMigrationProgressUpdateEvent(dto, progress, progress != 100));
        Runnable runnable = () -> {
            taskRepository.updateFullMigrationProgress(dto.getTaskId(), Math.round(progress));
        };
        if (async) executorService.submit(runnable);
        else runnable.run();
    }

    public double getProgress(int taskId) {
        return taskRepository.getOne(taskId).getFullMigrationProgress();
    }

    public boolean isTaskRunning(int taskId) {
        return runningTask.contains(taskId);
    }
}
