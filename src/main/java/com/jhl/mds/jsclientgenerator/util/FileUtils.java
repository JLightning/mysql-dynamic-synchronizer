package com.jhl.mds.jsclientgenerator.util;

import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class FileUtils {

    private Set<String> cleanedFile = new HashSet<>();

    public void initClean(String file) throws IOException {
        if (cleanedFile.contains(file)) return;

        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("// @flow\n");
        fileWriter.close();

        cleanedFile.add(file);
    }

    public void append(String filename, String content) throws IOException {
        FileWriter fileWriter = new FileWriter(filename, true);
        fileWriter.write(content);

        fileWriter.close();
    }
}
