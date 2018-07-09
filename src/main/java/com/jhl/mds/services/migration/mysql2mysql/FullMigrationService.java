package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.services.mysql.MySQLReadService;
import com.jhl.mds.services.mysql.MySQLWriteService;
import com.jhl.mds.util.Pipeline;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Service
public class FullMigrationService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private MySQLReadService mySQLReadService;
    private MySQLWriteService mySQLWriteService;
    private TaskRepository taskRepository;
    private MigrationMapperService.Factory migrationMapperServiceFactory;

    public FullMigrationService(
            MySQLReadService mySQLReadService,
            MySQLWriteService mySQLWriteService,
            TaskRepository taskRepository,
            MigrationMapperService.Factory migrationMapperServiceFactory
    ) {
        this.mySQLReadService = mySQLReadService;
        this.mySQLWriteService = mySQLWriteService;
        this.taskRepository = taskRepository;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
    }

    public void queue(FullMigrationDTO dto) {
        new Thread(() -> run(dto)).start();
    }

    public void run(FullMigrationDTO dto) {
        try {
            MigrationMapperService mapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
            List<String> targetColumns = mapperService.getColumns();

            dto.setTargetColumns(targetColumns);

            long count = mySQLReadService.count(dto.getSource());
            AtomicLong finished = new AtomicLong();

            final Consumer<Long> finishCallback = size -> {
                saveFullMigrationProgress(dto.getTaskId(), (double) (finished.addAndGet(size) * 100) / count);
                synchronized (finished) {
                    finished.notify();
                }
            };

            Pipeline<FullMigrationDTO, Long> pipeline = new Pipeline<>(dto);
            pipeline.setFinalNext(finishCallback);
            pipeline.setErrorHandler(e -> {
                if (e instanceof MySQLWriteService.WriteServiceException) {
                    finishCallback.accept(((MySQLWriteService.WriteServiceException) e).getCount());
                } else {
                    finishCallback.accept(1L);
                }
                saveFullMigrationProgress(dto.getTaskId(), (double) (finished.get() * 100) / count);
            });
            pipeline.append(mySQLReadService)
                    .append(mapperService)
                    .append(mySQLWriteService)
                    .execute();

            while (finished.get() < count) {
                synchronized (finished) {
                    finished.wait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFullMigrationProgress(int taskId, double v) {
        executorService.submit(() -> taskRepository.updateFullMigrationProgress(taskId, Math.round(v)));
    }

    public double getProgress(int taskId) {
        return taskRepository.getOne(taskId).getFullMigrationProgress();
    }
}
