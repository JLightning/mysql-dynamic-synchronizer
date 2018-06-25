package com.jhl.mds.dbmigration;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;

@Service
public class DatabaseMigration {

    private static final String FILENAME = "./db_migration.txt";

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
    private void postConstruct() {
        writeVersion("1.0.0");
    }
}
