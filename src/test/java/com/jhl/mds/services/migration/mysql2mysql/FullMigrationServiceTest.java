package com.jhl.mds.services.migration.mysql2mysql;

import com.jhl.mds.BaseTest;
import com.jhl.mds.consts.MySQLInsertMode;
import com.jhl.mds.dto.*;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class FullMigrationServiceTest extends BaseTest {

    private static final int LIMIT = 100000;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FullMigrationService fullMigrationService;

    @Autowired
    private MySQLConnectionPool mySQLConnectionPool;

    public void prepareData(Connection conn, Statement st) throws SQLException {
        st.execute("TRUNCATE mds.tablea;");
        st.execute("TRUNCATE mds.tableb;");

        Random rand = new Random();

        checkTime("all_test_insert", () -> {
            for (int j = 1; j <= LIMIT / 1000; j++) {
                try {
                    StringBuilder values = new StringBuilder();
                    for (int i = 1; i <= 1000; i++) {
                        if (values.length() != 0) values.append(", ");
                        int id = (j - 1) * 1000 + i;

                        values.append(String.format("(%d, %d)", id, rand.nextInt(10)));
                    }
                    final String finalValues = values.toString();
                    checkTime("test_insert_" + j, () -> {
                        try {
                            st.execute(String.format("INSERT INTO mds.tablea(`id`, `random_number`) VALUES %s;", finalValues));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {

                }
            }
        });
    }

    @Test
    public void runTest() throws Exception {
        MySQLServerDTO serverDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        Connection conn = mySQLConnectionPool.getConnection(serverDTO);
        Statement st = conn.createStatement();

        prepareData(conn, st);

        MigrationDTO dto = MigrationDTO.builder()
                .source(new TableInfoDTO(serverDTO, "mds", "tablea"))
                .target(new TableInfoDTO(serverDTO, "mds", "tableb"))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id + 1", "id"),
                        new SimpleFieldMappingDTO("random_number * 10", "random_number")
                ))
                .filters(Collections.singletonList("id % 2 == 1"))
                .insertMode(MySQLInsertMode.REPLACE)
                .build();

        checkTime("full_migration", () -> {
            try {
                fullMigrationService.run(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ResultSet result = st.executeQuery("SELECT COUNT(1) FROM mds.tableb");
        result.next();
        Assert.assertEquals(LIMIT / 2, result.getInt(1));
    }
}
