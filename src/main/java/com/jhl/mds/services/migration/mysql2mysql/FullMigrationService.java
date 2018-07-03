package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.services.mysql.MySQLReadService;
import com.jhl.mds.services.mysql.MySQLWriteService;
import com.jhl.mds.util.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class FullMigrationService {

    private static final int INSERT_CHUNK_SIZE = 1000;

    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static ExecutorService mappingExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
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

        List<String> insertDataList = new ArrayList<>();
        List<Future<?>> futures = new ArrayList<>();

        mySQLReadService.async(dto.getSource(), item -> {
            futures.add(mappingExecutor.submit(() -> {
                String mappedData = mapperService.mapToString(item);
                synchronized (insertDataList) {
                    insertDataList.add(mappedData);

                    if (insertDataList.size() == INSERT_CHUNK_SIZE) {
                        String insertDataStr = insertDataList.stream().collect(Collectors.joining(", "));
                        try {
                            mySQLWriteService.queue(dto.getTarget(), targetColumns, insertDataStr).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        insertDataList.clear();
                    }
                }
            }));
        }).get();

        if (insertDataList.size() > 0) {
            String insertDataStr = insertDataList.stream().collect(Collectors.joining(", "));
            futures.add(mySQLWriteService.queue(dto.getTarget(), targetColumns, insertDataStr));

            insertDataList.clear();
        }

        FutureUtil.allOf(futures);

        return true;
    }
}
