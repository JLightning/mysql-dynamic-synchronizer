package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.util.ColumnUtil;
import com.jhl.mds.util.FutureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class FullMigrationService {

    private static final int INSERT_CHUNK_SIZE = 1000;

    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private MySQLConnectionPool mySQLConnectionPool;
    private MySQLDescribeService mySQLDescribeService;
    private MySQLFieldDefaultValueService mySQLFieldDefaultValueService;
    private MySQLReadService mySQLReadService;
    private MySQLWriteService mySQLWriteService;

    public FullMigrationService(
            MySQLConnectionPool mySQLConnectionPool,
            MySQLDescribeService mySQLDescribeService,
            MySQLFieldDefaultValueService mySQLFieldDefaultValueService,
            MySQLReadService mySQLReadService,
            MySQLWriteService mySQLWriteService
    ) {
        this.mySQLConnectionPool = mySQLConnectionPool;
        this.mySQLDescribeService = mySQLDescribeService;
        this.mySQLFieldDefaultValueService = mySQLFieldDefaultValueService;
        this.mySQLReadService = mySQLReadService;
        this.mySQLWriteService = mySQLWriteService;
    }

    public Future<Boolean> queue(FullMigrationDTO dto) {
        return executor.submit(() -> run(dto));
    }

    public boolean run(FullMigrationDTO dto) throws SQLException {
        TaskDTO taskDTO = dto.getTaskDTO();
        List<TaskDTO.Mapping> mapping = taskDTO.getMapping();

        List<MySQLFieldDTO> targetFields = mySQLDescribeService.getFields(dto.getTarget(), taskDTO.getTarget().getDatabase(), taskDTO.getTarget().getTable());
        Map<String, MySQLFieldDTO> targetFieldMap = targetFields.stream().collect(Collectors.toMap(MySQLFieldDTO::getField, o -> o));

        List<String> sourceColumns = mapping.stream().map(TaskDTO.Mapping::getSourceField).collect(Collectors.toList());
        List<String> targetColumns = targetFields.stream().map(MySQLFieldDTO::getField).collect(Collectors.toList());

        Map<String, String> targetToSourceColumnMatch = mapping.stream().collect(Collectors.toMap(TaskDTO.Mapping::getTargetField, TaskDTO.Mapping::getSourceField));

        List<String> insertDataList = new ArrayList<>();
        List<Future<?>> futures = new ArrayList<>();

        Future<?> readFuture = mySQLReadService.async(dto.getSource(), taskDTO.getSource().getDatabase(), taskDTO.getSource().getTable(), sourceColumns, item -> {
            Map<String, Object> insertData = new LinkedHashMap<>();

            for (String targetColumn : targetColumns) {
                if (targetToSourceColumnMatch.containsKey(targetColumn)) {
                    insertData.put(targetColumn, item.get(targetToSourceColumnMatch.get(targetColumn)));
                } else {
                    insertData.put(targetColumn, mySQLFieldDefaultValueService.getDefaultValue(targetFieldMap.get(targetColumn)));
                }
            }

            insertDataList.add("(" + insertData.values().stream().map(o -> "'" + o.toString() + "'").collect(Collectors.joining(", ")) + ")");

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
