package com.jhl.mds.dbmigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class DatabaseMigrationRunner {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DataSource dataSource;

    public DatabaseMigrationRunner(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() throws SQLException {
        runMigration();
    }

    private void runMigration() throws SQLException {
        logger.info("Running Database Migration...");

        try (Connection conn = dataSource.getConnection()) {
            new DatabaseMigration100().run(conn);
            new DatabaseMigration101().run(conn);
            new DatabaseMigration102().run(conn);
        }
    }

    public static void start() {
        ConfigurableApplicationContext ctx = SpringApplication.run(DatabaseMigrationRunner.class);
        ctx.close();
    }
}
