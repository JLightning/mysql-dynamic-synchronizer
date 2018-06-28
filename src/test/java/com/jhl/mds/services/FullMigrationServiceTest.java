package com.jhl.mds.services;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.services.mysql.FullMigrationService;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FullMigrationServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FullMigrationService fullMigrationService;

    @Autowired
    private MySQLConnectionPool mySQLConnectionPool;

    public void prepareData() throws SQLException {
        MySQLServerDTO serverDTO = MySQLServerDTO.builder()
                .host("localhost")
                .port("3307")
                .username("root")
                .password("root")
                .build();

        Connection conn = mySQLConnectionPool.getConnection(serverDTO);

        Statement st = conn.createStatement();

        st.execute("TRUNCATE mds.tablea;");
        st.execute("TRUNCATE mds.tableb;");

        Random rand = new Random();

        checkTime("all_test_insert", () -> {
            for (int j = 1; j <= 10000; j++) {
                try {
                    StringBuilder values = new StringBuilder();
                    for (int i = 1; i <= 100; i++) {
                        if (values.length() != 0) values.append(", ");
                        int id = (j - 1) * 10000 + i;

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

        ResultSet result = st.executeQuery("SHOW ERRORS;");
        System.out.println("result = " + result);
    }

    private void checkTime(String task, Runnable r) {
        Instant start = Instant.now();
        r.run();
        System.out.println("elapsed time for `" + task + "` = " + Duration.between(start, Instant.now()));
    }

    @Test
    public void runTest() throws SQLException {
        prepareData();

        MySQLServerDTO serverDTO = MySQLServerDTO.builder()
                .host("localhost")
                .port("3307")
                .username("root")
                .password("root")
                .build();

        TaskDTO taskDTO = TaskDTO.builder()
                .mapping(Arrays.asList(
                        new TaskDTO.Mapping("id", "id"),
                        new TaskDTO.Mapping("random_number", "random_number")
                ))
                .source(new TaskDTO.Table(0, "mds", "tablea"))
                .target(new TaskDTO.Table(0, "mds", "tableb"))
                .build();

        FullMigrationDTO dto = FullMigrationDTO.builder()
                .source(serverDTO)
                .target(serverDTO)
                .taskDTO(taskDTO)
                .build();

        checkTime("full_migration", () -> {
            try {
                fullMigrationService.run(dto);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
