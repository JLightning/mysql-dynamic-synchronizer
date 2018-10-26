package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.dds.querybuilder.QueryBuilder;
import com.jhl.mds.dto.MySQLIndexDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import com.jhl.mds.util.Regex;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class StructureMigrationService implements PipeLineTaskRunner<MySQL2MySQLMigrationDTO, Object, Void> {

    private MySQLConnectionPool mySQLConnectionPool;
    private MySQLDescribeService mySQLDescribeService;

    public StructureMigrationService(
            MySQLConnectionPool mySQLConnectionPool,
            MySQLDescribeService mySQLDescribeService
    ) {
        this.mySQLConnectionPool = mySQLConnectionPool;
        this.mySQLDescribeService = mySQLDescribeService;
    }

//    CREATE TABLE `table_3498` (
//            `id` int(11) NOT NULL AUTO_INCREMENT,
//  `random_number` int(11) NOT NULL,
//  `random_text` varchar(255) DEFAULT NULL,
//  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
//            `created_at_tmp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
//    PRIMARY KEY (`id`)
//) ENGINE=InnoDB DEFAULT CHARSET=utf8

    // TODO: support more indexes
    @Override
    public void execute(MySQL2MySQLMigrationDTO context, Object input, Consumer<Void> next, Consumer<Exception> errorHandler) throws Exception {
        TableInfoDTO sourceTableInfo = context.getSource();
        TableInfoDTO targetTableInfo = context.getTarget();
        Connection targetConn = mySQLConnectionPool.getConnection(targetTableInfo.getServer());
        Statement st = targetConn.createStatement();

        ResultSet resultSet = st.executeQuery(new QueryBuilder().showCreateTable(sourceTableInfo.getDatabase(), sourceTableInfo.getTable()).build());
        if (!resultSet.next()) throw new RuntimeException("Cannot Show Create Table");

        List<String> createFieldStrs = new ArrayList<>();

        String createTableSql = resultSet.getString(2);
        for (SimpleFieldMappingDTO mapping : context.getMapping()) {
            String sourceField = mapping.getSourceField();
            String targetField = mapping.getTargetField();
            List<String> matches = Regex.findAllStringMatches(createTableSql, String.format("`%s`.*(?!DEFAULT|NULL).*,", sourceField));
            if (matches.size() == 0)
                throw new RuntimeException(String.format("Field %s not found in source table", sourceField));

            String match = matches.get(0);
            match = match.replaceAll(String.format("^`%s`", sourceField), String.format("`%s`", targetField));
            createFieldStrs.add(match.replaceAll(",$", ""));
        }

        List<String> engineStrs = Regex.findAllStringMatches(createTableSql, "\\) ENGINE=.*");
        if (engineStrs.size() == 0) throw new RuntimeException("Cannot find ENGINE for new table");
        String engineStr = engineStrs.get(0);

        List<MySQLIndexDTO> indexes = mySQLDescribeService.getIndexes(sourceTableInfo.getServer(), sourceTableInfo.getDatabase(), sourceTableInfo.getTable());
        Map<String, String> fieldSourceToTargetMapping = context.getMapping().stream().collect(Collectors.toMap(SimpleFieldMappingDTO::getSourceField, SimpleFieldMappingDTO::getTargetField));
        addIndexesToTable(indexes, fieldSourceToTargetMapping, createFieldStrs);

        String sql = String.format("CREATE TABLE `%s`.`%s`(%s%s",
                targetTableInfo.getDatabase(), targetTableInfo.getTable(), Strings.join(createFieldStrs, ','), engineStr);

        log.info("Run query: " + sql);

        st.execute(sql);
    }

    private void addIndexesToTable(List<MySQLIndexDTO> indexes, Map<String, String> fieldSourceToTargetMapping, List<String> createFieldStrs) {
        for (MySQLIndexDTO indexDTO : indexes) {
            if (fieldSourceToTargetMapping.containsKey(indexDTO.getColumnName())) {
                createFieldStrs.add(indexToCreateTableString(indexDTO, fieldSourceToTargetMapping));
            }
        }
    }

    private String indexToCreateTableString(MySQLIndexDTO indexDTO, Map<String, String> mapping) {
        String mappedColumn = mapping.get(indexDTO.getColumnName());
        if (indexDTO.getKeyName().equals("PRIMARY")) {
            return String.format("PRIMARY KEY (`%s`)", mappedColumn);
        } else if (!indexDTO.isNonUnique()) {
            return String.format("UNIQUE `%s` (`%s` %s)", indexDTO.getKeyName(), mappedColumn, indexDTO.getCollation().equals("A") ? "ASC" : "DESC");
        }
        return String.format("INDEX `%s` (`%s` %s)", indexDTO.getKeyName(), mappedColumn, indexDTO.getCollation().equals("A") ? "ASC" : "DESC");
    }
}
