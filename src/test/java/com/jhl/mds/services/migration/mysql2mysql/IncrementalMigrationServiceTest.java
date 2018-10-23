package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.TableTemplate;
import com.jhl.mds.consts.MySQLInsertMode;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.migration.MySQL2MySQLMigrationDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class IncrementalMigrationServiceTest extends BaseTest {

    @Autowired
    private IncrementalMigrationService incrementalMigrationService;

    @Test
    public void insertTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = getBaseDTOBuilder(sourceTable, targetTable)
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

        List<Map<String, Object>> sourceResult = readAndConvertToListOfMap("SELECT * FROM mds." + sourceTable);
        List<Map<String, Object>> targetResult = readAndConvertToListOfMap("SELECT * FROM mds." + targetTable);

        Assert.assertEquals(sourceResult.size(), targetResult.size());

        for (int i = 0;i<sourceResult.size();i++) {
            Map<String, Object> sourceRow = sourceResult.get(i);
            Map<String, Object> targetRow = targetResult.get(i);

            Assert.assertEquals(sourceRow.get("random_number"), targetRow.get("random_number"));
            Assert.assertEquals(sourceRow.get("random_text"), targetRow.get("random_text"));
            Assert.assertEquals(sourceRow.get("created_at"), targetRow.get("created_at"));
        }

        incrementalMigrationService.stop(dto);
    }

    public List<Map<String, Object>> readAndConvertToListOfMap(String sql) throws SQLException {
        ResultSet resultSet = getStatement().executeQuery(sql);
        List<Map<String, Object>> result = new ArrayList<>();

        while (resultSet.next()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }

            result.add(row);
        }

        return result;
    }

    private MySQL2MySQLMigrationDTO.MySQL2MySQLMigrationDTOBuilder getBaseDTOBuilder(String sourceTable, String targetTable) {
        return MySQL2MySQLMigrationDTO.builder()
                .taskId(randomTaskId())
                .source(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", sourceTable))
                .target(new TableInfoDTO(getSourceMySQLServerDTO(), "mds", targetTable))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number", "random_number"),
                        new SimpleFieldMappingDTO("random_text", "random_text"),
                        new SimpleFieldMappingDTO("created_at", "created_at")
                ))
                .migrationActionCode(0b111)
                .insertMode(MySQLInsertMode.REPLACE);
    }

    @Test
    public void updateTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = getBaseDTOBuilder(sourceTable, targetTable)
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds." + sourceTable + "(`random_number`, `random_text`) VALUES (2, 'James')");
        }
        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = 8");

        Thread.sleep(2000);

        ResultSet result = getStatement().executeQuery("SELECT * FROM mds." + targetTable);
        while (result.next()) {
            Assert.assertEquals(8, result.getInt(2));
        }

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateWithFilterDeleteNeededTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = getBaseDTOBuilder(sourceTable, targetTable)
                .filters(Collections.singletonList("random_number % 2 == 0"))
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

        result = getStatement().executeQuery("SELECT * FROM mds." + targetTable);
        while (result.next()) {
            Assert.assertEquals(0, result.getInt(2) % 2);
        }

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateWithFilterInsertNeededTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = getBaseDTOBuilder(sourceTable, targetTable)
                .filters(Collections.singletonList("random_number % 2 == 0"))
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

        result = getStatement().executeQuery("SELECT * FROM mds." + targetTable);
        while (result.next()) {
            Assert.assertEquals(0, result.getInt(2) % 2);
        }

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void updateWithFilterInsertDeleteNeededTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = getBaseDTOBuilder(sourceTable, targetTable)
                .filters(Collections.singletonList("random_number % 2 == 0"))
                .build();

        incrementalMigrationService.run(dto);

        Thread.sleep(500);

        for (int i = 0; i < 100; i++) {
            getStatement().execute(String.format("INSERT INTO mds." + sourceTable + "(`random_number`) VALUES (%d)", i));
        }
        getStatement().execute("UPDATE mds." + sourceTable + " SET random_number = random_number - 1");

        Thread.sleep(2000);

        ResultSet result = getStatement().executeQuery("SELECT COUNT(1) FROM mds." + targetTable);
        result.next();
        Assert.assertEquals(50, result.getInt(1));

        result = getStatement().executeQuery("SELECT * FROM mds." + targetTable);
        while (result.next()) {
            Assert.assertEquals(0, result.getInt(2) % 2);
        }

        incrementalMigrationService.stop(dto);
    }

    @Test
    public void deleteTest() throws Exception {
        String sourceTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);
        String targetTable = prepareTable(TableTemplate.TEMPLATE_SIMPLE);

        MySQL2MySQLMigrationDTO dto = getBaseDTOBuilder(sourceTable, targetTable)
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