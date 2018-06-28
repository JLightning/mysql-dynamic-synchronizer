package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.util.ColumnUtil;
import com.jhl.mds.util.FutureUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
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
    private MySQLService mySQLService;
    private MySQLFieldDefaultValueService mySQLFieldDefaultValueService;
    private MySQLWriteService mySQLWriteService;

    public FullMigrationService(
            MySQLConnectionPool mySQLConnectionPool,
            MySQLService mySQLService,
            MySQLFieldDefaultValueService mySQLFieldDefaultValueService,
            MySQLWriteService mySQLWriteService
    ) {
        this.mySQLConnectionPool = mySQLConnectionPool;
        this.mySQLService = mySQLService;
        this.mySQLFieldDefaultValueService = mySQLFieldDefaultValueService;
        this.mySQLWriteService = mySQLWriteService;
    }

    public Future<Boolean> queue(FullMigrationDTO dto) {
        return executor.submit(() -> run(dto));
    }

    public boolean run(FullMigrationDTO dto) throws SQLException {
        Statement sourceSt = mySQLConnectionPool.getConnection(dto.getSource()).createStatement();

        TaskDTO taskDTO = dto.getTaskDTO();
        List<TaskDTO.Mapping> mapping = taskDTO.getMapping();

        List<MySQLFieldDTO> targetFields = mySQLService.getFields(dto.getTarget(), taskDTO.getTarget().getDatabase(), taskDTO.getTarget().getTable());
        Map<String, MySQLFieldDTO> targetFieldMap = targetFields.stream().collect(Collectors.toMap(MySQLFieldDTO::getField, o -> o));

        List<String> targetColumns = targetFields.stream().map(MySQLFieldDTO::getField).collect(Collectors.toList());
        List<String> sourceColumns = mapping.stream().map(TaskDTO.Mapping::getSourceField).collect(Collectors.toList());

        Map<String, String> targetToSourceColumnMatch = mapping.stream().collect(Collectors.toMap(TaskDTO.Mapping::getTargetField, TaskDTO.Mapping::getSourceField));

        String sql = String.format("SELECT %s FROM %s;", ColumnUtil.columnListToString(sourceColumns), taskDTO.getSource().getDatabase() + "." + taskDTO.getSource().getTable());
        ResultSet result = sourceSt.executeQuery(sql);

        List<String> insertDataList = new ArrayList<>();
        List<Future<?>> futures = new ArrayList<>();
        while (result.next()) {
            Map<String, Object> data = new HashMap<>();
            for (int i = 0; i < sourceColumns.size(); i++) {
                String sourceColumn = sourceColumns.get(i);
                data.put(sourceColumn, result.getObject(i + 1));
            }

            Map<String, Object> insertData = new LinkedHashMap<>();

            for (String targetColumn : targetColumns) {
                if (targetToSourceColumnMatch.containsKey(targetColumn)) {
                    insertData.put(targetColumn, data.get(targetToSourceColumnMatch.get(targetColumn)));
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
