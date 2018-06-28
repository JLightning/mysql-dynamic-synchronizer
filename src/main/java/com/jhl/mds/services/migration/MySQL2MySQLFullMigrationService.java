package com.jhl.mds.services.migration;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.services.mysql.MySQLReadService;
import com.jhl.mds.services.mysql.MySQLWriteService;
import com.jhl.mds.util.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class MySQL2MySQLFullMigrationService {

    private static final int INSERT_CHUNK_SIZE = 1000;

    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private MySQLReadService mySQLReadService;
    private MySQLWriteService mySQLWriteService;
    private MySQL2MySQLMigrationMapperService.Factory migrationMapperServiceFactory;

    public MySQL2MySQLFullMigrationService(
            MySQLReadService mySQLReadService,
            MySQLWriteService mySQLWriteService,
            MySQL2MySQLMigrationMapperService.Factory migrationMapperServiceFactory
    ) {
        this.mySQLReadService = mySQLReadService;
        this.mySQLWriteService = mySQLWriteService;
        this.migrationMapperServiceFactory = migrationMapperServiceFactory;
    }

    public Future<Boolean> queue(FullMigrationDTO dto) {
        return executor.submit(() -> run(dto));
    }

    public boolean run(FullMigrationDTO dto) throws SQLException {
        TaskDTO taskDTO = dto.getTaskDTO();
        List<TaskDTO.Mapping> mapping = taskDTO.getMapping();

        List<String> sourceColumns = mapping.stream().map(TaskDTO.Mapping::getSourceField).collect(Collectors.toList());

        MySQL2MySQLMigrationMapperService mySQL2MySQLMigrationMapperService = migrationMapperServiceFactory.create(dto.getTarget(), taskDTO.getTarget().getDatabase(), taskDTO.getTarget().getTable(), taskDTO.getMapping());
        List<String> targetColumns = mySQL2MySQLMigrationMapperService.getColumns();

        List<String> insertDataList = new ArrayList<>();
        List<Future<?>> futures = new ArrayList<>();

        Future<?> readFuture = mySQLReadService.async(dto.getSource(), taskDTO.getSource().getDatabase(), taskDTO.getSource().getTable(), sourceColumns, item -> {
            insertDataList.add(mySQL2MySQLMigrationMapperService.mapToString(item));

            if (insertDataList.size() == INSERT_CHUNK_SIZE) {
                String insertDataStr = insertDataList.stream().collect(Collectors.joining(", "));
                futures.add(mySQLWriteService.queue(dto.getTarget(), taskDTO.getTarget().getDatabase(), taskDTO.getTarget().getTable(), targetColumns, insertDataStr));

                insertDataList.clear();
            }
        });

        try {
            readFuture.get();
        } catch (Exception e) {
        }

        if (insertDataList.size() > 0) {
            String insertDataStr = insertDataList.stream().collect(Collectors.joining(", "));
            mySQLWriteService.queue(dto.getTarget(), taskDTO.getTarget().getDatabase(), taskDTO.getTarget().getTable(), targetColumns, insertDataStr);

            insertDataList.clear();
        }

        FutureUtil.allOf(futures);

        return true;
    }
}
