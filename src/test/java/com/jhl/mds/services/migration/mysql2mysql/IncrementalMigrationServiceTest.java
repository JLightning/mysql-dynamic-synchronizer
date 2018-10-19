package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.consts.MySQLInsertMode;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
public class IncrementalMigrationServiceTest extends BaseTest {

    @Autowired
    private IncrementalMigrationService incrementalMigrationService;

    @Test
    public void insertTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQLServerDTO serverDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        MySQL2MySQLMigrationDTO dto = MySQL2MySQLMigrationDTO.builder()
                .taskId(randomTaskId())
                .source(new TableInfoDTO(serverDTO, "mds", sourceTable))
                .target(new TableInfoDTO(serverDTO, "mds", targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number * 2", "random_number")
                ))
                .migrationActionCode(0b111)
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

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = MySQL2MySQLMigrationDTO.builder()
                .taskId(randomTaskId())
                .source(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", sourceTable))
                .target(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number * 2", "random_number")
                ))
                .migrationActionCode(0b111)
                .insertMode(MySQLInsertMode.REPLACE)
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (2)");
        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = 8");

        Thread.sleep(2000);

        ResultSet result = getStatement().executeQuery("SELECT * FROM mds." + targetTable);
        result.next();
        Assert.assertEquals(16, result.getInt(2));

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateWithFilterDeleteNeededTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = MySQL2MySQLMigrationDTO.builder()
                .taskId(randomTaskId())
                .source(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", sourceTable))
                .target(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number", "random_number")
                ))
                .migrationActionCode(0b111)
                .filters(Collections.singletonList("random_number % 2 == 0"))
                .insertMode(MySQLInsertMode.REPLACE)
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute(String.format("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (%d)", 4));
        }
        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = id");

        Thread.sleep(2000);

        ResultSet result = getStatement().executeQuery("SELECT COUNT(1) FROM mds." + targetTable);
        result.next();
        Assert.assertEquals(50, result.getInt(1));

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateWithFilterInsertNeededTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = MySQL2MySQLMigrationDTO.builder()
                .taskId(randomTaskId())
                .source(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", sourceTable))
                .target(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number", "random_number")
                ))
                .migrationActionCode(0b111)
                .filters(Collections.singletonList("random_number % 2 == 0"))
                .insertMode(MySQLInsertMode.REPLACE)
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute(String.format("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (%d)", 3));
        }
        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = id");

        Thread.sleep(2000);

        ResultSet result = getStatement().executeQuery("SELECT COUNT(1) FROM mds." + targetTable);
        result.next();
        Assert.assertEquals(50, result.getInt(1));

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void deleteTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = MySQL2MySQLMigrationDTO.builder()
                .taskId(randomTaskId())
                .source(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", sourceTable))
                .target(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number * 2", "random_number")
                ))
                .migrationActionCode(0b111)
                .insertMode(MySQLInsertMode.REPLACE)
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute(String.format("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (%d)", i));
        }
        getStatement().execute("DELETE FROM mds." + sourceTable);

        Thread.sleep(2000);

        ResultSet result = getStatement().executeQuery("SELECT * FROM mds." + targetTable);
        Assert.assertFalse(result.next());

        incrementalMigrationService.stop(dto);
    }
}