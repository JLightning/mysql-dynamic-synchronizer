package com.jhl.mds.dbmigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class DatabaseMigrationRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DataSource dataSource;

    @Autowired
    public DatabaseMigrationRunner(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
    }

    @PostConstruct
    private void init() throws SQLException {
        runMigration();
    }

    private void runMigration() throws SQLException {
        logger.info("Running Database Migration...");

        try (Connection conn = dataSource.getConnection()) {
            new DatabaseMigration100().run(conn);
        }
    }
}
