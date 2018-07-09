package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.services.mysql.MySQLReadService;
import com.jhl.mds.services.mysql.MySQLWriteService;
import com.jhl.mds.util.Pipeline;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Service
public class FullMigrationService {

    private MySQLReadService mySQLReadService;
    private MySQLWriteService mySQLWriteService;
    private MigrationMapperService.Factory migrationMapperServiceFactory;
    private Map<Integer, Double> progressList = new HashMap<>();

    public FullMigrationService(
            MySQLReadService mySQLReadService,
            MySQLWriteService mySQLWriteService,
            MigrationMapperService.Factory migrationMapperServiceFactory
    ) {
        this.mySQLReadService = mySQLReadService;
        this.mySQLWriteService = mySQLWriteService;
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
                progressList.put(dto.getTaskId(), (double) (finished.addAndGet(size) * 100) / count);
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
                progressList.put(dto.getTaskId(), (double) (finished.get() * 100) / count);
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

    public double getProgress(int taskId) {
        return progressList.getOrDefault(taskId, 0d);
    }
}
