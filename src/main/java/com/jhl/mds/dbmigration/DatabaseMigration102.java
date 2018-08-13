package com.jhl.mds.dbmigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration102 extends AbstractDatabaseMigration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    void run(Connection conn) throws SQLException {
        Statement st = conn.createStatement();
        if (getCurrentVersion().compareTo("1.0.2") < 0) {
            logger.info("Database Migration Version: 1.0.2");

            st.execute("DROP TABLE IF EXISTS task_statistics;");
            st.execute("CREATE TABLE task_statistics (" +
                    "task_statistics_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "fk_task_id INTEGER," +
                    "insert_count INTEGER," +
                    "update_count INTEGER," +
                    "delete_count INTEGER," +
                    "created_at TIMESTAMP," +
                    "updated_at TIMESTAMP," +
                    "FOREIGN KEY(fk_task_id) REFERENCES task(task_id) ON DELETE CASCADE," +
                    "CONSTRAINT task UNIQUE (fk_task_id)" +
                    ");");

            writeVersion("1.0.2");
        }
    }
}
