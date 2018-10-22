package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.consts.MySQLConstants;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;
import com.jhl.mds.services.customefilter.CustomFilterService;
import com.jhl.mds.services.mysql.MySQLInsertService;
import com.jhl.mds.services.mysql.MySQLReadService;
import com.jhl.mds.services.task.TaskStatisticService;
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

    private MySQLReadService mySQLReadService;
    private CustomFilterService customFilterService;
    private MySQLInsertService mySQLInsertService;
    private TaskStatisticService taskStatisticService;
    private TaskRepository taskRepository;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private Set<Integer> runningTask = new HashSet<>();

    public FullMigrationService(
            MySQLReadService mySQLReadService,
            CustomFilterService customFilterService,
            MySQLInsertService mySQLInsertService,
            TaskStatisticService taskStatisticService,
            TaskRepository taskRepository,
            MigrationMapperService.Factory migrationMapperServiceFactory
    ) {
        this.mySQLReadService = mySQLReadService;
        this.customFilterService = customFilterService;
        this.mySQLInsertService = mySQLInsertService;
        this.taskStatisticService = taskStatisticService;
        this.taskRepository = taskRepository;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
    }

    public void queue(MySQL2MySQLMigrationDTO dto) {
        if (runningTask.contains(dto.getTaskId())) {
            throw new RuntimeException("Task has already been running");
        }
        runningTask.add(dto.getTaskId());
        taskStatisticService.updateTaskFullMigrationProgress(dto.getTaskId(), 0);
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
                        taskStatisticService.updateTaskFullMigrationProgress(dto.getTaskId(), progress);
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

    public double getProgress(int taskId) {
        return taskRepository.getOne(taskId).getFullMigrationProgress();
    }

    public boolean isTaskRunning(int taskId) {
        return runningTask.contains(taskId);
    }
}
