package com.jhl.mds.jsclientgenerator;

import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class FileCleaner {

    private Set<String> cleanedFile = new HashSet<>();

    public void clean(String file) throws IOException {
        if (cleanedFile.contains(file)) return;

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("// @flow\n");
        fileWriter.close();

        cleanedFile.add(file);
    }
}
