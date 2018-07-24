package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.events.MigrationProgressUpdateEvent;
import com.jhl.mds.services.customefilter.CustomFilterService;
import com.jhl.mds.services.mysql.MySQLReadService;
import com.jhl.mds.services.mysql.MySQLWriteService;
import com.jhl.mds.util.Pipeline;
import com.jhl.mds.util.PipelineGrouperService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FullMigrationService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private MySQLReadService mySQLReadService;
    private CustomFilterService customFilterService;
    private MySQLWriteService mySQLWriteService;
    private TaskRepository taskRepository;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private ApplicationEventPublisher eventPublisher;
    private Set<Integer> runningTask = new HashSet<>();

    public FullMigrationService(
            MySQLReadService mySQLReadService,
            CustomFilterService customFilterService,
            MySQLWriteService mySQLWriteService,
            TaskRepository taskRepository,
            MigrationMapperService.Factory migrationMapperServiceFactory,
            ApplicationEventPublisher eventPublisher
    ) {
        this.mySQLReadService = mySQLReadService;
        this.customFilterService = customFilterService;
        this.mySQLWriteService = mySQLWriteService;
        this.taskRepository = taskRepository;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
        this.eventPublisher = eventPublisher;
    }

    public void queue(FullMigrationDTO dto) {
        if (runningTask.contains(dto.getTaskId())) {
            throw new RuntimeException("Task has already been running");
        }
        runningTask.add(dto.getTaskId());
        saveFullMigrationProgress(dto, 0, false);
        new Thread(() -> run(dto)).start();
    }

    public void run(FullMigrationDTO dto) {
        try {
            MigrationMapperService mapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            dto.setTargetColumns(mapperService.getColumns());

            Pipeline<FullMigrationDTO, Long> pipeline = new Pipeline<>(dto);
            pipeline.append(mySQLReadService)
                    .append(customFilterService)
                    .append(mapperService)
                    .append(new PipelineGrouperService<String>(1234))
                    .append(mySQLWriteService)
                    .execute()
                    .waitForFinish();

        } catch (Exception e) {
            e.printStackTrace();
        }


        runningTask.remove(dto.getTaskId());
    }

    private void saveFullMigrationProgress(FullMigrationDTO dto, double progress, boolean async) {
        eventPublisher.publishEvent(new MigrationProgressUpdateEvent(dto, progress));

        Runnable runnable = () -> taskRepository.updateFullMigrationProgress(dto.getTaskId(), Math.round(progress));
        if (async) executorService.submit(runnable);
        else runnable.run();
    }

    public double getProgress(int taskId) {
        return taskRepository.getOne(taskId).getFullMigrationProgress();
    }
}
