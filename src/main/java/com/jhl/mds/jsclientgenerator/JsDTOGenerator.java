package com.jhl.mds.jsclientgenerator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public abstract class JsDTOGenerator {

    static final String BASE_CLIENT_JS_DIRECTORY = "./src/main/resources/static/resources/js/source/dto/";
    private TemplateReader templateReader;

    public JsDTOGenerator(TemplateReader templateReader) {
        this.templateReader = templateReader;
    }

    String renderClass(String className, List<String> fields, List<String> constructorParameters, List<String> constructorSetters, String constructorComment, String methods) {
        String renderClassContent = templateReader.getDtoClassTemplate().replaceAll("\\{className}", className);
        renderClassContent = renderClassContent.replaceAll("\\{fields}", StringUtils.join(fields, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_parameters}", StringUtils.join(constructorParameters, ", "));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_setters}", StringUtils.join(constructorSetters, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_comment}", constructorComment);
        renderClassContent = renderClassContent.replaceAll("\\{methods}", methods);

        return renderClassContent;
    }
}
