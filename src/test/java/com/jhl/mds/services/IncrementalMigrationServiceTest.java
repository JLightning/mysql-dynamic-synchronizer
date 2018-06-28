package com.jhl.mds.services;

import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.services.migration.mysql2mysql.IncrementalMigrationService;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IncrementalMigrationServiceTest {

    @Autowired
    private IncrementalMigrationService incrementalMigrationService;

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
    }

    @Test
    public void runTest() throws Exception {
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

        incrementalMigrationService.async(dto);

        Thread.sleep(1000);

        Connection conn = mySQLConnectionPool.getConnection(serverDTO);

        Statement st = conn.createStatement();
        st.execute("INSERT INTO mds.tablea(`random_number`) VALUES (1)");

        Thread.sleep(1000 * 1000);
    }
}
