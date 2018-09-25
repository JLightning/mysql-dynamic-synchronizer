package com.jhl.mds.dbmigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration103 extends AbstractDatabaseMigration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    void run(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        if (getCurrentVersion().compareTo("1.0.3") < 0) {
            logger.info("Database Migration Version: 1.0.0");

            st.execute("DROP TABLE IF EXISTS redis_server;");
            st.execute("CREATE TABLE redis_server (" +
                    "server_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name VARCHAR(1024) UNIQUE," +
                    "host VARCHAR(255)," +
                    "port VARCHAR(255)," +
                    "username VARCHAR(127)," +
                    "password VARCHAR(127)," +
                    "created_at TIMESTAMP," +
                    "updated_at TIMESTAMP" +
                    ");");
            st.execute("CREATE UNIQUE INDEX redis_server_hpup ON redis_server(host, port, username, password);");

            writeVersion("1.0.3");
        }
    }
}
