package com.jhl.mds.jsclientgenerator;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JsJsDTOClassGenerator extends JsDTOGenerator {

    private TemplateReader templateReader;
    private JsJsDTOEnumGenerator jsDTOEnumGenerator;
    private TypeCommentGenerator typeCommentGenerator;
    private DTORegistry dtoRegistry;
    private FileUtils fileUtils;
    private Map<Class, String> generated = new HashMap<>();
    private Class processing = null;

    public JsJsDTOClassGenerator(TemplateReader templateReader, JsJsDTOEnumGenerator jsDTOEnumGenerator, TypeCommentGenerator typeCommentGenerator, DTORegistry dtoRegistry, FileUtils fileUtils) {
        super(templateReader);
        this.templateReader = templateReader;
        this.jsDTOEnumGenerator = jsDTOEnumGenerator;
        this.typeCommentGenerator = typeCommentGenerator;
        this.dtoRegistry = dtoRegistry;
        this.fileUtils = fileUtils;
        typeCommentGenerator.setJsDTOClassGenerator(this);
    }

    public void start() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(JsClientDTO.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.jhl.mds")) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());

            generateDto(clazz, null);
        }
    }

    public String generateDto(Class<?> clazz, String appendToFileIfAnnotationNotFound) throws IOException {
        if (clazz.isEnum()) return jsDTOEnumGenerator.generateDto(clazz, appendToFileIfAnnotationNotFound);

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

        fileUtils.initClean(BASE_CLIENT_JS_DIRECTORY + fileName + ".js");

        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldStr = new ArrayList<>();
        List<String> constructorParameters = new ArrayList<>();
        List<String> constructorSetters = new ArrayList<>();
        for (Field field : fields) {
            String defaultValue = getDefaultValueForField(field);

            String type = typeCommentGenerator.getTypeComment(field, fileName);

            String renderedField = templateReader.getDtoFieldTemplate().replaceAll("\\{field}", field.getName() + " : ?" + type);

            renderedField = renderedField.replaceAll("\\{type}", type);
            renderedField = renderedField.replaceAll("\\{default_value}", defaultValue);
            fieldStr.add(renderedField);

            constructorParameters.add(field.getName() + " : ?" + type);

            constructorSetters.add(templateReader.getDtoConstructorSetterTemplate().replaceAll("\\{parameter}", field.getName()));
        }

        String renderedClass = renderClass(className, fieldStr, constructorParameters, constructorSetters, typeCommentGenerator.renderMethodComment(fields, fileName), "");

        fileUtils.append(BASE_CLIENT_JS_DIRECTORY + fileName + ".js", renderedClass);

        generated.put(clazz, className);
        processing = null;

        dtoRegistry.addTmpGenerated(new DTORegistry.GeneratedDefinition(className, BASE_CLIENT_JS_DIRECTORY + fileName + ".js"));
        return className;
    }

    private String getDefaultValueForField(Field field) {
        String defaultValue = "null";
        if (field.getType().isPrimitive()) {
            defaultValue = "0";
            if (field.getType() == boolean.class) {
                defaultValue = "false";
            } else if (field.getType() == char.class) {
                defaultValue = "''";
            }
        } else if (field.getType().getName().equals("java.lang.String")) {
            return "''";
        }
        return defaultValue;
    }
}
