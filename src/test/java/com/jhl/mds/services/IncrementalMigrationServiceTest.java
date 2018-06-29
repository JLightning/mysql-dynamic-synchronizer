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
        MySQLServerDTO serverDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        Connection conn = mySQLConnectionPool.getConnection(serverDTO);

        Statement st = conn.createStatement();

        st.execute("TRUNCATE mds.tablea;");
        st.execute("TRUNCATE mds.tableb;");
    }

    @Test
    public void insertTest() throws Exception {
        prepareData();

        MySQLServerDTO serverDTO = new MySQLServerDTO(0, "test", "localhost", "3307", "root", "root");

        FullMigrationDTO dto = FullMigrationDTO.builder()
                .source(new TableInfoDTO(serverDTO, "mds", "tablea"))
                .target(new TableInfoDTO(serverDTO, "mds", "tableb"))
                .mapping(Arrays.asList(
                        new SimpleFieldMappingDTO("id", "id"),
                        new SimpleFieldMappingDTO("random_number", "random_number")
                ))
                .build();

        LifecycleListener connected = new LifecycleListener();
        incrementalMigrationService.async(dto, connected);

        synchronized (connected) {
            while (!connected.get()) {
                connected.wait();
            }
        }

        Connection conn = mySQLConnectionPool.getConnection(serverDTO);

        Statement st = conn.createStatement();
        for (int i = 0; i < 10; i++) {
            st.execute("INSERT INTO mds.tablea(`random_number`) VALUES (1)");
        }

        Thread.sleep(500);

        ResultSet result = st.executeQuery("SELECT COUNT(1) FROM mds.tableb");
        result.next();
        Assert.assertEquals(10, result.getInt(1));
    }

    private class LifecycleListener extends AtomicBoolean implements BinaryLogClient.LifecycleListener {
        @Override
        public synchronized void onConnect(BinaryLogClient client) {z
            set(true);
            notify();
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
    }
}
