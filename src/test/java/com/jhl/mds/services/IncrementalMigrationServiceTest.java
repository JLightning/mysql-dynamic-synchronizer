package com.jhl.mds.services;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.jhl.mds.BaseTest;
import com.jhl.mds.dto.FullMigrationDTO;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.services.migration.mysql2mysql.IncrementalMigrationService;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class IncrementalMigrationServiceTest extends BaseTest {

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

        FullMigrationDTO dto = FullMigrationDTO.builder()
                .source(new TableInfoDTO(serverDTO, "mds", "tablea"))
                .target(new TableInfoDTO(serverDTO, "mds", "tableb"))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id", "id"),
                        new SimpleFieldMappingDTO("random_number", "random_number")
                ))
                .build();

        AtomicBoolean connected = new AtomicBoolean(false);

        incrementalMigrationService.async(dto, new BinaryLogClient.LifecycleListener() {
            @Override
            public void onConnect(BinaryLogClient client) {
                synchronized (connected) {
                    connected.set(true);
                    connected.notify();
                }
            }

            @Override
            public void onCommunicationFailure(BinaryLogClient client, Exception ex) {

            }

            @Override
            public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {

            }

            @Override
            public void onDisconnect(BinaryLogClient client) {

            }
        });

        synchronized (connected) {
            while (!connected.get()) {
                connected.wait();
            }
        }

        Connection conn = mySQLConnectionPool.getConnection(serverDTO);

        Statement st = conn.createStatement();
        st.execute("INSERT INTO mds.tablea(`random_number`) VALUES (1)");
        st.execute("INSERT INTO mds.tablea(`random_number`) VALUES (2)");

        Thread.sleep(500);

        ResultSet result = st.executeQuery("SELECT COUNT(1) FROM mds.tableb");
        result.next();
        Assert.assertEquals(2, result.getInt(1));
    }
}
