package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class IncrementalMigrationServiceTest extends BaseTest {

    @Autowired
    private IncrementalMigrationService incrementalMigrationService;

    public void prepareData() throws SQLException {
        getStatement().execute("TRUNCATE mds.tablea;");
        getStatement().execute("TRUNCATE mds.tableb;");
    }

    @Test
    public void insertTest() throws Exception {
        prepareData();

        MySQLServerDTO serverDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        FullMigrationDTO dto = FullMigrationDTO.builder()
                .source(new TableInfoDTO(serverDTO, "mds", "tablea"))
                .target(new TableInfoDTO(serverDTO, "mds", "tableb"))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number * 2", "random_number")
                ))
                .build();

        incrementalMigrationService.run(dto);

        for (int i = 0; i < 100; i++) {
            getStatement().execute("INSERT INTO mds.tablea(`random_number`) VALUES (1)");
        }

        Thread.sleep(2000);

        ResultSet result = getStatement().executeQuery("SELECT COUNT(1) FROM mds.tableb");
        result.next();
        Assert.assertEquals(100, result.getInt(1));
    }

    @Test
    public void updateTest() throws Exception {
        prepareData();

        FullMigrationDTO dto = FullMigrationDTO.builder()
                .source(new TableInfoDTO(getSourceServerDTO(), "mds", "tablea"))
                .target(new TableInfoDTO(getSourceServerDTO(), "mds", "tableb"))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number * 2", "random_number")
                ))
                .build();

        incrementalMigrationService.run(dto);

        getStatement().execute("INSERT INTO mds.tablea(`random_number`) VALUES (2)");
        getStatement().execute("UPDATE mds.tablea SET random_number = 8");

        Thread.sleep(500);

        ResultSet result = getStatement().executeQuery("SELECT * FROM mds.tableb");
        result.next();
        Assert.assertEquals(16, result.getInt(2));
    }
}