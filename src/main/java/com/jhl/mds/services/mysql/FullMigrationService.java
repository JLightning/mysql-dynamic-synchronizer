package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.TaskDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class FullMigrationService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static ExecutorService executor = Executors.newFixedThreadPool(4);
    private MySQLConnectionPool mySQLConnectionPool;

    public FullMigrationService(MySQLConnectionPool mySQLConnectionPool) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    public Future<Boolean> queue(FullMigrationDTO dto) {
        return executor.submit(() -> run(dto));
    }

    public boolean run(FullMigrationDTO dto) throws SQLException {
        Statement sourceSt = mySQLConnectionPool.getConnection(dto.getSource()).createStatement();
        Statement targetSt = mySQLConnectionPool.getConnection(dto.getTarget()).createStatement();

        TaskDTO taskDTO = dto.getTaskDTO();
        List<TaskDTO.Mapping> mapping = taskDTO.getMapping();

        List<String> targetColumns = mapping.stream().map(TaskDTO.Mapping::getTargetField).collect(Collectors.toList());
        String targetColumnsStr = targetColumns.stream().map(o -> "`" + o + "`").collect(Collectors.joining(", "));

        List<String> sourceColumns = mapping.stream().map(TaskDTO.Mapping::getSourceField).collect(Collectors.toList());
        String sourceColumnsStr = sourceColumns.stream().collect(Collectors.joining(", "));

        Map<String, String> targetToSourceColumnMatch = mapping.stream().collect(Collectors.toMap(TaskDTO.Mapping::getTargetField, TaskDTO.Mapping::getSourceField));

        String sql = String.format("SELECT %s FROM %s;", sourceColumnsStr, taskDTO.getSource().getDatabase() + "." + taskDTO.getSource().getTable());
        ResultSet result = sourceSt.executeQuery(sql);

        StringBuilder insertDataStr = new StringBuilder();
        while (result.next()) {
            Map<String, Object> data = new HashMap<>();
            for (int i = 0; i < sourceColumns.size(); i++) {
                String sourceColumn = sourceColumns.get(i);
                data.put(sourceColumn, result.getObject(i + 1));
            }

            Map<String, Object> insertData = new LinkedHashMap<>();

            for (String targetColumn : targetColumns) {
                insertData.put(targetColumn, data.get(targetToSourceColumnMatch.get(targetColumn)));
            }

            if (insertDataStr.length() != 0) insertDataStr.append(", ");
            insertDataStr.append("(" + insertData.values().stream().map(o -> "'" + o.toString() + "'").collect(Collectors.joining(", ")) + ")");
        }

        sql = String.format("INSERT INTO %s(%s) VALUES %s;", taskDTO.getTarget().getDatabase() + "." + taskDTO.getTarget().getTable(), targetColumnsStr, insertDataStr.toString());
        logger.info("Run query: " + sql);

        try {
            targetSt.execute(sql);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return true;
    }
}
