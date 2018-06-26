package com.jhl.mds.dbmigration;

import java.io.*;

public class AbstractDatabaseMigration {

    private static final String FILENAME = "./db_migration.txt";

    String getCurrentVersion() {
        String version = "0.0.0";
        try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {
            version = br.readLine().trim();
        } finally {
            return version;
        }
    }

    void writeVersion(String version) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {
            bw.write(version);
        } catch (IOException e) {

        }
    }
}
