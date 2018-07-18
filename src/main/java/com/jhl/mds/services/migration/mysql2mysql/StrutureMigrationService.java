package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import com.jhl.mds.util.PipeLineTaskRunner;
import com.jhl.mds.util.Regex;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class StrutureMigrationService implements PipeLineTaskRunner<FullMigrationDTO, Object, Void> {

    private MySQLConnectionPool mySQLConnectionPool;

    public StrutureMigrationService(
            MySQLConnectionPool mySQLConnectionPool
    ) {
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    //    CREATE TABLE `task` (
//  `task_id` int(11) NOT NULL AUTO_INCREMENT,
//  `task_name` varchar(1024) NOT NULL,
//  `task_code` varchar(128) NOT NULL,
//  `fk_source_database` int(11) NOT NULL,
//  `source_table` varchar(1024) NOT NULL,
//  `fk_target_database` int(11) NOT NULL,
//  `target_table` int(11) NOT NULL,
//  `created_at` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP,
//  `updated_at` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP,
//   PRIMARY KEY (`task_id`)
//) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1
    @Override
    public void execute(FullMigrationDTO context, Object input, Consumer<Void> next, Consumer<Exception> errorHandler) throws Exception {
        List<SimpleFieldMappingDTO> mapping = context.getMapping();
        Map<String, String> fieldSourceToTargetMap = mapping.stream().collect(Collectors.toMap(SimpleFieldMappingDTO::getSourceField, SimpleFieldMappingDTO::getTargetField));

        Connection sourceConn = mySQLConnectionPool.getConnection(context.getSource().getServer());
        Statement sourceSt = sourceConn.createStatement();

        Connection targetConn = mySQLConnectionPool.getConnection(context.getTarget().getServer());
        Statement targetSt = targetConn.createStatement();

        ResultSet result = sourceSt.executeQuery(String.format("SHOW CREATE TABLE %s.%s", context.getSource().getDatabase(), context.getSource().getTable()));
        result.next();

        String createTableQuery = result.getString(2);

        String[] arr = createTableQuery.split("\n");
        List<String> newQueryArr = new ArrayList<>();

        newQueryArr.add(arr[0].replaceAll("`" + context.getSource().getTable() + "`", "`" + context.getTarget().getTable() + "`"));
        for (int i = 1; i < arr.length; i++) {
            String line = arr[i].trim();
            List<List<String>> fieldNameRegexResult = Regex.findAllStringSubmatches(line, "`(.*)`");
            boolean already = false;
            if (fieldNameRegexResult.size() == 1 && fieldNameRegexResult.get(0).size() == 2) {
                String fieldName = fieldNameRegexResult.get(0).get(1);
                if (fieldSourceToTargetMap.containsKey(fieldName)) {
                    newQueryArr.add(line.replaceAll("`" + fieldName + "`", "`" + fieldSourceToTargetMap.get(fieldName) + "`"));
                    already = true;
                }
            }
            if (!line.startsWith("`") && !already) {
                newQueryArr.add(line);
            }
        }

        createTableQuery = Strings.join(newQueryArr, ' ');

        System.out.println("createTableQuery = " + createTableQuery);

        targetSt.execute("USE " + context.getTarget().getDatabase());
        targetSt.execute(createTableQuery);
    }
}
