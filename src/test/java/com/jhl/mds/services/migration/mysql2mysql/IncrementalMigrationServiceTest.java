package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.consts.MySQLInsertMode;
import com.jhl.mds.dto.migration.MigrationDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.ResultSet;
import java.util.Arrays;

public class IncrementalMigrationServiceTest extends BaseTest {

    @Autowired
    private IncrementalMigrationService incrementalMigrationService;

    @Test
    public void insertTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQLServerDTO serverDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        MigrationDTO dto = MigrationDTO.builder()
                .taskId((int) (Math.random() * 10000))
                .source(new TableInfoDTO(serverDTO, "mds", sourceTable))
                .target(new TableInfoDTO(serverDTO, "mds", targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number * 2", "random_number")
                ))
                .insertMode(MySQLInsertMode.REPLACE)
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (1)");
        }

        Thread.sleep(2000);

        ResultSet result = getStatement().executeQuery("SELECT COUNT(1) FROM mds." + targetTable);
        result.next();
        Assert.assertEquals(100, result.getInt(1));
    }

    @Test
    public void updateTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MigrationDTO dto = MigrationDTO.builder()
                .taskId((int) (Math.random() * 10000))
                .source(new TableInfoDTO(getSourceServerDTO(), "mds", sourceTable))
                .target(new TableInfoDTO(getSourceServerDTO(), "mds", targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number * 2", "random_number")
                ))
                .insertMode(MySQLInsertMode.REPLACE)
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (2)");
        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = 8");

        Thread.sleep(500);

        ResultSet result = getStatement().executeQuery("SELECT * FROM mds." + targetTable);
        result.next();
        Assert.assertEquals(16, result.getInt(2));
    }
}