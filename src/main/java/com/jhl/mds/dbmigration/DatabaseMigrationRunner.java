package com.jhl.mds.dbmigration;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SpringBootApplication
public class DatabaseMigrationRunner {

    private static final String DB_VERSION_FILENAME = "./db_migration.txt";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DataSource dataSource;

    public DatabaseMigrationRunner(DataSource dataSource) throws SQLException {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() throws SQLException, IOException {
        runMigration();
    }

    private void runMigration() throws SQLException, IOException {
        logger.info("Running Database Migration...");

        try (Connection conn = dataSource.getConnection()) {
            Statement st = conn.createStatement();

            List<File> migrationFiles = new ArrayList<>();

            ClassLoader classLoader = getClass().getClassLoader();
            File folder = new File(classLoader.getResource("db-migration").getFile());
            if (folder.isDirectory()) {
                for (String migrationFileName : folder.list()) {
                    migrationFiles.add(new File(classLoader.getResource("db-migration/" + migrationFileName).getFile()));
                }
            }

            migrationFiles.sort(Comparator.comparing(o -> getVersionFromFilename(o.getName())));

            for (File migrationFile : migrationFiles) {
                runMigrationForFile(st, migrationFile);
            }
        }
    }

    private void runMigrationForFile(Statement st, File migrationFile) throws IOException, SQLException {
        if (getCurrentVersion().compareTo(getVersionFromFilename(migrationFile.getName())) < 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(migrationFile))) {
                String sql = "";
                String line;
                while ((line = br.readLine()) != null) {
                    sql += line;
                    if (line.trim().endsWith(";")) {
                        st.execute(sql);
                        sql = "";

                        writeVersion(getVersionFromFilename(migrationFile.getName()));
                    }
                }
            }
        }
    }

    private String getVersionFromFilename(String name) {
        String[] arr = name.split("-");
        return arr[0];
    }

    public static void start() {
        ConfigurableApplicationContext ctx = SpringApplication.run(DatabaseMigrationRunner.class);
        ctx.close();
    }

    private static String getCurrentVersion() {
        String version = "0.0.0";
        try {
            version = FileUtils.readFileToString(new File(DB_VERSION_FILENAME), "utf-8");
        } finally {
            return version;
        }
    }

    private static void writeVersion(String version) {
        try {
            FileUtils.writeStringToFile(new File(DB_VERSION_FILENAME), version, "utf-8");
        } catch (IOException e) {

        }
    }
}
