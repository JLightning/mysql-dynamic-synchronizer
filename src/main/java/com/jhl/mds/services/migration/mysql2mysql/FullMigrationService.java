package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.services.mysql.MySQLReadService;
import com.jhl.mds.services.mysql.MySQLWriteService;
import com.jhl.mds.util.Pipeline;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Service
public class FullMigrationService {

    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private MySQLReadService mySQLReadService;
    private MySQLWriteService mySQLWriteService;
    private MigrationMapperService.Factory migrationMapperServiceFactory;

    public FullMigrationService(
            MySQLReadService mySQLReadService,
            MySQLWriteService mySQLWriteService,
            MigrationMapperService.Factory migrationMapperServiceFactory
    ) {
        this.mySQLReadService = mySQLReadService;
        this.mySQLWriteService = mySQLWriteService;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
    }

    public Future<Boolean> queue(FullMigrationDTO dto) {
        return executor.submit(() -> run(dto));
    }

    public boolean run(FullMigrationDTO dto) throws Exception {
        MigrationMapperService mapperService = migrationMapperServiceFactory.create(dto.getTarget(), dto.getMapping());
        List<String> targetColumns = mapperService.getColumns();

        dto.setTargetColumns(targetColumns);

//        mySQLReadService.run(dto.getSource(), item -> mapperService.queueMapToString(item, mappedData -> mySQLWriteService.queue(
//                dto.getTarget(),
//                new MySQLWriteService.WriteInfo(targetColumns, mappedData, () -> finishCallback.accept(1L))
//        )));
//

        long count = mySQLReadService.count(dto.getSource());
        AtomicLong finished = new AtomicLong();

        final Consumer<Long> finishCallback = size -> {
            finished.addAndGet(size);
            synchronized (finished) {
                finished.notify();
            }
        };

        Pipeline<FullMigrationDTO, Long> pipeline = new Pipeline<>(dto);
        pipeline.setFinalNext(finishCallback);
        pipeline.append(mySQLReadService)
                .append(mapperService)
                .append(mySQLWriteService)
                .execute();

        while (finished.get() < count) {
            synchronized (finished) {
                finished.wait();
            }
        }

        return true;
    }
}
