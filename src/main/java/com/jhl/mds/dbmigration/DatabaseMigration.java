package com.jhl.mds.dbmigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Service
public class DatabaseMigration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String FILENAME = "./db_migration.txt";
    private final DataSource dataSource;

    @Autowired
    public DatabaseMigration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private String getCurrentVersion() {
        String version = "0.0.0";
        try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {
            version = br.readLine().trim();
        } finally {
            return version;
        }
    }

    private void writeVersion(String version) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {
            bw.write(version);
        } catch (IOException e) {

        }
    }

    @PostConstruct
    private void postConstruct() throws SQLException {
        runMigration();
    }

    private void runMigration() throws SQLException {
        logger.info("Running Database Migration...");

        Connection conn = dataSource.getConnection();
        Statement st = conn.createStatement();
        if (getCurrentVersion().compareTo("1.0.0") < 0) {
            logger.info("Database Migration Version: 1.0.0");

            st.execute("DROP TABLE IF EXISTS mysql_server;");
            st.execute("CREATE TABLE mysql_server (" +
                    "server_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name VARCHAR(1024) UNIQUE," +
                    "host VARCHAR(255)," +
                    "port VARCHAR(255)," +
                    "username VARCHAR(127)," +
                    "password VARCHAR(127)," +
                    "created_at TIMESTAMP," +
                    "updated_at TIMESTAMP" +
                    ");");
            st.execute("CREATE UNIQUE INDEX mysql_server_hpup ON mysql_server(host, port, username, password);");

            st.execute("DROP TABLE IF EXISTS task;");
            st.execute("CREATE TABLE task (" +
                    "task_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name VARCHAR(1024) UNIQUE," +
                    "fk_source_server INTEGER," +
                    "source_database VARCHAR(255)," +
                    "source_table VARCHAR(255)," +
                    "fk_target_server INTEGER," +
                    "target_database VARCHAR(255)," +
                    "target_table VARCHAR(255)," +
                    "created_at TIMESTAMP," +
                    "updated_at TIMESTAMP," +
                    "FOREIGN KEY(fk_source_server) REFERENCES mysql_server(server_id) ON UPDATE CASCADE," +
                    "FOREIGN KEY(fk_target_server) REFERENCES mysql_server(server_id) ON UPDATE CASCADE" +
                    ");");

            st.execute("DROP TABLE IF EXISTS task_field_mapping;");
            st.execute("CREATE TABLE task_field_mapping (" +
                    "mapping_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "fk_task_id INTEGER," +
                    "source_field VARCHAR(255)," +
                    "target_field VARCHAR(255)," +
                    "created_at TIMESTAMP," +
                    "updated_at TIMESTAMP," +
                    "FOREIGN KEY(fk_task_id) REFERENCES task(task_id) ON UPDATE CASCADE" +
                    ");");

            writeVersion("1.0.0");
        }

        conn.close();
    }
}
