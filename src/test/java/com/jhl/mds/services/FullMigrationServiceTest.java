package com.jhl.mds.services;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.services.mysql.FullMigrationService;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FullMigrationServiceTest {

    @Autowired
    private FullMigrationService fullMigrationService;

    @Autowired
    private MySQLConnectionPool mySQLConnectionPool;

    public void prepareData() throws SQLException {
        Connection conn = mySQLConnectionPool.getConnection(MySQLServerDTO.builder()
                .host("localhost")
                .port("3307")
                .username("root")
                .password("root")
                .build());

        Statement st = conn.createStatement();
        st.execute("TRUNCATE mds.tablea;");
        st.execute("TRUNCATE mds.tableb;");
        String values = "";
        for (int i = 1; i <= 10000; i++) {
            if (!values.equals("")) values += ", ";
            values += String.format("(%d, %d)", i, System.currentTimeMillis() % 100000);
        }
        final String finalValues = values;
        checkTime(() -> {
            try {
                st.execute(String.format("INSERT INTO mds.tablea(`id`, `random_number`) VALUES %s;", finalValues));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void checkTime(Runnable r) {
        Instant start = Instant.now();
        r.run();
        System.out.println("elapsed time = " + Duration.between(start, Instant.now()));
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
                .mapping(Arrays.asList(TaskDTO.Mapping.builder()
                                .sourceField("id")
                                .targetField("id")
                                .build(),
                        TaskDTO.Mapping.builder()
                                .sourceField("random_number")
                                .targetField("random_number")
                                .build()
                ))
                .source(TaskDTO.Table.builder()
                        .database("mds")
                        .table("tablea")
                        .build()
                )
                .target(TaskDTO.Table.builder()
                        .database("mds")
                        .table("tableb")
                        .build())
                .build();

        FullMigrationDTO dto = FullMigrationDTO.builder()
                .source(serverDTO)
                .target(serverDTO)
                .taskDTO(taskDTO)
                .build();

        checkTime(() -> {
            try {
                fullMigrationService.run(dto);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
