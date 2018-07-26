package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.dto.*;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import com.jhl.mds.util.pipeline.PipeLineTaskRunner;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class StructureMigrationService implements PipeLineTaskRunner<MigrationDTO, Object, Void> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private MySQLConnectionPool mySQLConnectionPool;
    private MySQLDescribeService mySQLDescribeService;

    public StructureMigrationService(
            MySQLConnectionPool mySQLConnectionPool,
            MySQLDescribeService mySQLDescribeService
    ) {
        this.mySQLConnectionPool = mySQLConnectionPool;
        this.mySQLDescribeService = mySQLDescribeService;
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

    // TODO: support more indexes
    @Override
    public void execute(MigrationDTO context, Object input, Consumer<Void> next, Consumer<Exception> errorHandler) throws Exception {
        TableInfoDTO targetTableInfo = context.getTarget();
        Connection targetConn = mySQLConnectionPool.getConnection(targetTableInfo.getServer());
        Statement st = targetConn.createStatement();

        TableInfoDTO sourceTableInfo = context.getSource();
        List<MySQLFieldDTO> fields = mySQLDescribeService.getFields(sourceTableInfo.getServer(), sourceTableInfo.getDatabase(), sourceTableInfo.getTable());
        List<MySQLIndexDTO> indexes = mySQLDescribeService.getIndexes(sourceTableInfo.getServer(), sourceTableInfo.getDatabase(), sourceTableInfo.getTable());

        Map<String, String> fieldSourceToTargetMapping = context.getMapping().stream().collect(Collectors.toMap(SimpleFieldMappingDTO::getSourceField, SimpleFieldMappingDTO::getTargetField));

        List<String> createFieldStrs = new ArrayList<>();
        for (MySQLFieldDTO fieldDTO : fields) {
            if (fieldSourceToTargetMapping.containsKey(fieldDTO.getField())) {
                createFieldStrs.add(fieldToCreateTableString(fieldDTO, fieldSourceToTargetMapping));
            }
        }

        addIndexesToTable(indexes, fieldSourceToTargetMapping, createFieldStrs);

        String sql = String.format("CREATE TABLE `%s`.`%s`(%s) ENGINE=InnoDB DEFAULT CHARSET=latin1",
                targetTableInfo.getDatabase(), targetTableInfo.getTable(), Strings.join(createFieldStrs, ','));

        logger.info("Run query: " + sql);

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

    private String fieldToCreateTableString(MySQLFieldDTO fieldDTO, Map<String, String> mapping) {
        String defaultStr = fieldDTO.getDefaultValue() != null ? "DEFAULT " + fieldDTO.getDefaultValue() : "";
        String commentStr = fieldDTO.getComment() != null ? "COMMENT '" + fieldDTO.getComment() + "'" : "";
        return String.format("`%s` %s %s %s %s %s", mapping.get(fieldDTO.getField()), fieldDTO.getType(), fieldDTO.isNullable() ? "" : "NOT NULL", defaultStr, fieldDTO.getExtra(), commentStr);
    }
}
