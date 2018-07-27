package com.jhl.mds.dbmigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration101 extends AbstractDatabaseMigration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    void run(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        if (getCurrentVersion().compareTo("1.0.1") < 0) {
            logger.info("Database Migration Version: 1.0.1");

            st.execute("DROP TABLE IF EXISTS task_filter;");
            st.execute("CREATE TABLE task_filter (" +
                    "filter_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "fk_task_id INTEGER," +
                    "filter VARCHAR(255)," +
                    "created_at TIMESTAMP," +
                    "updated_at TIMESTAMP," +
                    "FOREIGN KEY(fk_task_id) REFERENCES task(task_id) ON DELETE CASCADE," +
                    "CONSTRAINT task_filter UNIQUE (fk_task_id, filter)" +
                    ");");

            writeVersion("1.0.1");
        }
    }
}
