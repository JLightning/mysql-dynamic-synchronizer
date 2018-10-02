package com.jhl.mds.jsclientgenerator;

import com.jhl.mds.jsclientgenerator.util.FileUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

@Service
public class JsJsDTOEnumGenerator extends JsDTOGenerator {

    private TemplateReader templateReader;
    private JsClassImportRegistry jsClassImportRegistry;
    private FileUtils fileUtils;
    private Class processing = null;

    public JsJsDTOEnumGenerator(TemplateReader templateReader, JsClassImportRegistry jsClassImportRegistry, FileUtils fileUtils) {
        super(templateReader);
        this.templateReader = templateReader;
        this.jsClassImportRegistry = jsClassImportRegistry;
        this.fileUtils = fileUtils;
    }

    public String generateDto(Class<?> clazz) throws IOException {
        JsClientDTO jsClientDTO = clazz.getAnnotation(JsClientDTO.class);

        Pair<String, String> pair;
        try {
            pair = getFileNameAndClassName(jsClientDTO, clazz);
        } catch (Exception e) {
            return "";
        }

        String fileName = pair.getFirst();
        String className = pair.getSecond();

        if (clazz == processing) return className;
        processing = clazz;

        fileUtils.initClean(BASE_CLIENT_JS_DIRECTORY + fileName + ".js");

        List<Field> fields = Arrays.asList(clazz.getFields());
        List<String> fieldStr = new ArrayList<>();
        List<String> constructorParameters = new ArrayList<>();
        List<String> constructorSetters = new ArrayList<>();

        for (Field field : fields) {
            String renderedField = templateReader.getDtoFieldTemplate().replaceAll("\\{field}", "static " + field.getName() + " : ?" + className);
            renderedField = renderedField.replaceAll("\\{type}", className);
            renderedField = renderedField.replaceAll("\\{default_value}", "null");

            fieldStr.add(renderedField);
        }

        String renderedField = templateReader.getDtoFieldTemplate().replaceAll("\\{field}", "name : string");
        renderedField = renderedField.replaceAll("\\{type}", "string");
        renderedField = renderedField.replaceAll("\\{default_value}", "''");
        fieldStr.add(renderedField);

        constructorParameters.add("name : string");
        constructorSetters.add(templateReader.getDtoConstructorSetterTemplate().replaceAll("\\{parameter}", "name"));

        String renderedClass = renderClass(className, fieldStr, constructorParameters, constructorSetters, renderMethodComment(fields, fileName), getToJson(className));
        renderedClass = renderedClass.replaceAll("\n\n\n", "\n");

        renderedClass = renderedClass.replaceAll("\\{imports}", "");

        renderedClass += "\n";
        for (Field field : fields) {
            renderedClass += className + "." + field.getName() + " = new " + className + "('" + field.getName() + "');\n";
        }
        renderedClass += "\n";

        fileUtils.append(BASE_CLIENT_JS_DIRECTORY + fileName + ".js", renderedClass);

        processing = null;

        jsClassImportRegistry.addGeneratedClass(clazz, new JsClassImportRegistry.GeneratedDefinition(className, BASE_CLIENT_JS_DIRECTORY + fileName + ".js"));
        jsClassImportRegistry.addImportMap(new JsClassImportRegistry.GeneratedDefinition(className, BASE_CLIENT_JS_DIRECTORY + fileName + ".js"));

        return className;
    }

    private String getToJson(String className) {
        return "     toJSON() {\n          return this.name;\n     }\n\n";
    }

    private String renderMethodComment(List<Field> fields, String fileName) {
        String renderedParam = templateReader.getMethodCommentTemplateParam().replaceAll("\\{param}", "name");
        renderedParam = renderedParam.replaceAll("\\{type}", "string");

        return templateReader.getMethodCommentTemplate().replaceAll("\\{params}", renderedParam);
    }
}
