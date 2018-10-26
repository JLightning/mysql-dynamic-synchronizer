package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.dto.MySQLFieldDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;
import com.jhl.mds.services.mysql.MySQLDescribeService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

public class StructureMigrationServiceTest extends BaseTest {

    @Autowired
    private StructureMigrationService structureMigrationService;

    @Autowired
    private MySQLDescribeService mySQLDescribeService;

    @Test
    public void executeTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = generateRandomTableName();

        getStatement().execute("USE mds");
        getStatement().execute("DROP TABLE IF EXISTS " + targetTable);

        MySQL2MySQLMigrationDTO dto = MySQL2MySQLMigrationDTO.builder()
                .source(new TableInfoDTO(getSourceMySQLServerDTO(), TEST_DATABASE, sourceTable))
                .target(new TableInfoDTO(getSourceMySQLServerDTO(), TEST_DATABASE, targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id", "id_tmp"),
                        new SimpleFieldMappingDTO("random_number", "random_number_tmp")
                ))
                .build();

        structureMigrationService.execute(dto, null, null, null);

        Thread.sleep(500);

        List<MySQLFieldDTO> fields = mySQLDescribeService.getFields(getSourceMySQLServerDTO(), TEST_DATABASE, targetTable);
        Assert.assertEquals(2, fields.size());
        Assert.assertEquals("id_tmp", fields.get(0).getField());
        Assert.assertEquals("random_number_tmp", fields.get(1).getField());

        addCreatedTable(targetTable);
    }
}
