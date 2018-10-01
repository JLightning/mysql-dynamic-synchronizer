package com.jhl.mds.jsclientgenerator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

@Service
public class JsDTOEnumGenerator {

    private static final String BASE_CLIENT_JS_DIRECTORY = "./src/main/resources/static/resources/js/source/dto/";
    private TemplateReader templateReader;
    private FileCleaner fileCleaner;
    private Map<Class, String> generated = new HashMap<>();
    private Class processing = null;

    public JsDTOEnumGenerator(TemplateReader templateReader, FileCleaner fileCleaner) {
        this.templateReader = templateReader;
        this.fileCleaner = fileCleaner;
    }

    public String generateDto(Class<?> clazz, String appendToFileIfAnnotationNotFound) throws IOException {
        if (generated.containsKey(clazz)) return generated.get(clazz);
        JsClientDTO jsClientDTO = clazz.getAnnotation(JsClientDTO.class);

        String className, fileName;
        if (jsClientDTO != null) {
            className = jsClientDTO.className();
            fileName = jsClientDTO.fileName();
        } else if (appendToFileIfAnnotationNotFound != null) {
            className = clazz.getSimpleName();
            fileName = appendToFileIfAnnotationNotFound;
        } else {
            return "";
        }

        if (clazz == processing) return className;
        processing = clazz;

        fileCleaner.clean(BASE_CLIENT_JS_DIRECTORY + fileName + ".js");

        List<Field> fields = Arrays.asList(clazz.getFields());
        List<String> fieldStr = new ArrayList<>();
        List<String> constructorParameters = new ArrayList<>();
        List<String> constructorSetters = new ArrayList<>();

        String renderedField = templateReader.getDtoFieldTemplate().replaceAll("\\{field}", "name");
        renderedField = renderedField.replaceAll("\\{type}", "string");
        renderedField = renderedField.replaceAll("\\{default_value}", "''");
        fieldStr.add(renderedField);

        constructorParameters.add("name");
        constructorSetters.add(templateReader.getDtoConstructorSetterTemplate().replaceAll("\\{parameter}", "name"));

        String renderedClass = renderClass(className, fieldStr, constructorParameters, constructorSetters, renderMethodComment(fields, fileName));
        renderedClass = renderedClass.replaceAll("\n\n\n", "\n");

        if (appendToFileIfAnnotationNotFound != null && jsClientDTO == null) {
            renderedClass = renderedClass.replaceAll("export default ", "export ");
        }

        for (Field field : fields) {
            renderedClass += "\n" + className + "." + field.getName() + " = new " + className + "('" + field.getName() + "');";
        }

        renderedClass += "\n" + getToJson(className);

        FileWriter fileWriter = new FileWriter(BASE_CLIENT_JS_DIRECTORY + fileName + ".js", true);
        fileWriter.append(renderedClass);

        fileWriter.close();
        generated.put(clazz, className);
        processing = null;
        return className;
    }

    private String getToJson(String className) {
        return className + ".prototype.toJSON = function() { return this.name;};\n\n";
    }

    private String renderMethodComment(List<Field> fields, String fileName) {
        String renderedParam = templateReader.getMethodCommentTemplateParam().replaceAll("\\{param}", "name");
        renderedParam = renderedParam.replaceAll("\\{type}", "string");

        return templateReader.getMethodCommentTemplate().replaceAll("\\{params}", renderedParam);
    }

    private String renderClass(String className, List<String> fields, List<String> constructorParameters, List<String> constructorSetters, String constructorComment) {
        String renderClassContent = templateReader.getDtoClassTemplate().replaceAll("\\{className}", className);
        renderClassContent = renderClassContent.replaceAll("\\{fields}", StringUtils.join(fields, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_parameters}", StringUtils.join(constructorParameters, ", "));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_setters}", StringUtils.join(constructorSetters, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_comment}", constructorComment);

        return renderClassContent;
    }
}
