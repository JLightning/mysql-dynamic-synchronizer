package com.jhl.mds.dbmigration;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class AbstractDatabaseMigration {

    private static final String FILENAME = "./db_migration.txt";

    String getCurrentVersion() {
        String version = "0.0.0";
        try {
            version = FileUtils.readFileToString(new File(FILENAME), "utf-8");
        } finally {
            return version;
        }
    }

    void writeVersion(String version) {
        try {
            FileUtils.writeStringToFile(new File(FILENAME), version, "utf-8");
        } catch (IOException e) {

        }
    }
}
