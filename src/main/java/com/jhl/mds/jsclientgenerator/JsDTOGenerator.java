package com.jhl.mds.jsclientgenerator;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JsDTOGenerator {

    private static final String BASE_CLIENT_JS_DIRECTORY = "./src/main/resources/static/resources/js/source/dto/";
    private TemplateReader templateReader;
    private JsDTOEnumGenerator jsDTOEnumGenerator;
    private TypeCommentGenerator typeCommentGenerator;
    private DTORegister dtoRegister;
    private FileCleaner fileCleaner;
    private Map<Class, String> generated = new HashMap<>();
    private Class processing = null;

    public JsDTOGenerator(TemplateReader templateReader, JsDTOEnumGenerator jsDTOEnumGenerator, TypeCommentGenerator typeCommentGenerator, DTORegister dtoRegister, FileCleaner fileCleaner) {
        this.templateReader = templateReader;
        this.jsDTOEnumGenerator = jsDTOEnumGenerator;
        this.typeCommentGenerator = typeCommentGenerator;
        this.dtoRegister = dtoRegister;
        this.fileCleaner = fileCleaner;
        typeCommentGenerator.setJsDTOGenerator(this);
    }

    @PostConstruct
    public void init() throws Exception {
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

        fileCleaner.clean(BASE_CLIENT_JS_DIRECTORY + fileName + ".js");

        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldStr = new ArrayList<>();
        List<String> constructorParameters = new ArrayList<>();
        List<String> constructorSetters = new ArrayList<>();
        for (Field field : fields) {
            String defaultValue = getDefaultValueForField(field);

            String type;
            try {
                type = typeCommentGenerator.getTypeComment(field.getType(), (ParameterizedType) field.getGenericType(), fileName);
            } catch (ClassCastException e) {
                type = typeCommentGenerator.getTypeComment(field.getType(), null, fileName);
            }

            String renderedField = templateReader.getDtoFieldTemplate().replaceAll("\\{field}", field.getName() + " : ?" + type);

            renderedField = renderedField.replaceAll("\\{type}", type);
            renderedField = renderedField.replaceAll("\\{default_value}", defaultValue);
            fieldStr.add(renderedField);

            constructorParameters.add(field.getName() + " : ?" + type);

            constructorSetters.add(templateReader.getDtoConstructorSetterTemplate().replaceAll("\\{parameter}", field.getName()));
        }

        String renderedClass = renderClass(className, fieldStr, constructorParameters, constructorSetters, typeCommentGenerator.renderMethodComment(fields, fileName));

        if (appendToFileIfAnnotationNotFound != null && jsClientDTO == null) {
            renderedClass = renderedClass.replaceAll("export default ", "export ");
        }

        FileWriter fileWriter = new FileWriter(BASE_CLIENT_JS_DIRECTORY + fileName + ".js", true);
        fileWriter.append(renderedClass);

        fileWriter.close();
        generated.put(clazz, className);
        processing = null;

        dtoRegister.addTmpGenerated(new DTORegister.GeneratedDefinition(className, fileName));
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

    private String renderClass(String className, List<String> fields, List<String> constructorParameters, List<String> constructorSetters, String constructorComment) {
        String renderClassContent = templateReader.getDtoClassTemplate().replaceAll("\\{className}", className);
        renderClassContent = renderClassContent.replaceAll("\\{fields}", StringUtils.join(fields, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_parameters}", StringUtils.join(constructorParameters, ", "));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_setters}", StringUtils.join(constructorSetters, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_comment}", constructorComment);
        renderClassContent = renderClassContent.replaceAll("\\{methods}", "");

        return renderClassContent;
    }
}
