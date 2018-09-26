package com.jhl.mds.jsclientgenerator;

import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

@Service
public class TemplateReader {

    private static final String TEMPLATE_FILE = "static/resources/js/source/client.js.template";
    @Getter
    private String classTemplate = "";
    @Getter
    private String methodTemplate = "";

    @PostConstruct
    public void readTemplate() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File templateFile = new File(classLoader.getResource(TEMPLATE_FILE).getFile());

        StringBuilder classTemplateBuilder = new StringBuilder();
        StringBuilder methodTemplateBuilder = new StringBuilder();
        boolean isClassTemplate = false;
        BufferedReader br = new BufferedReader(new FileReader(templateFile));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.equals("[class_template]")) {
                isClassTemplate = true;
                continue;
            } else if (line.equals("[method_template]")) {
                isClassTemplate = false;
                continue;
            }
            if (isClassTemplate) {
                if (classTemplateBuilder.length() > 0) classTemplateBuilder.append("\n");
                classTemplateBuilder.append(line);
            } else {
                if (methodTemplateBuilder.length() > 0) methodTemplateBuilder.append("\n");
                methodTemplateBuilder.append(line);
            }
        }
        classTemplate = classTemplateBuilder.toString();
        methodTemplate = methodTemplateBuilder.toString();
    }
}
