package com.jhl.mds.jsclientgenerator;

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
    private Map<Class, String> generated = new HashMap<>();
    private Class processing = null;

    public JsDTOGenerator(TemplateReader templateReader, JsDTOEnumGenerator jsDTOEnumGenerator) {
        this.templateReader = templateReader;
        this.jsDTOEnumGenerator = jsDTOEnumGenerator;
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

    private String generateDto(Class<?> clazz, String appendToFileIfAnnotationNotFound) throws IOException {
        if (clazz.isEnum()) return jsDTOEnumGenerator.generateDto(clazz, appendToFileIfAnnotationNotFound);
        if (clazz == processing) return "*";
        processing = clazz;

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

        if (appendToFileIfAnnotationNotFound == null || jsClientDTO != null) {
            FileWriter fileWriter = new FileWriter(BASE_CLIENT_JS_DIRECTORY + fileName + ".js");
            fileWriter.close();
        }

        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldStr = new ArrayList<>();
        List<String> constructorParameters = new ArrayList<>();
        List<String> constructorSetters = new ArrayList<>();
        for (Field field : fields) {
            String defaultValue = "null";
            if (field.getType() == int.class) {
                defaultValue = "0";
            }

            String renderedField = templateReader.getDtoFieldTemplate().replaceAll("\\{field}", field.getName());
            renderedField = renderedField.replaceAll("\\{type}", getTypeComment(field.getType(), field, fileName));
            renderedField = renderedField.replaceAll("\\{default_value}", defaultValue);
            fieldStr.add(renderedField);

            constructorParameters.add(field.getName());

            constructorSetters.add(templateReader.getDtoConstructorSetterTemplate().replaceAll("\\{parameter}", field.getName()));
        }

        String renderedClass = renderClass(className, fieldStr, constructorParameters, constructorSetters, renderMethodComment(fields, fileName));

        if (appendToFileIfAnnotationNotFound != null && jsClientDTO == null) {
            renderedClass = renderedClass.replaceAll("export default ", "export ");
        }

        FileWriter fileWriter = new FileWriter(BASE_CLIENT_JS_DIRECTORY + fileName + ".js", true);
        fileWriter.append(renderedClass);

        fileWriter.close();
        generated.put(clazz, className);
        processing = null;
        return className;
    }

    private String renderMethodComment(Field[] fields, String fileName) {
        List<String> params = new ArrayList<>();
        for (Field field : fields) {
            String renderedParam = templateReader.getMethodCommentTemplateParam().replaceAll("\\{param}", field.getName());
            renderedParam = renderedParam.replaceAll("\\{type}", getTypeComment(field.getType(), field, fileName));

            params.add(renderedParam);
        }

        return templateReader.getMethodCommentTemplate().replaceAll("\\{params}", StringUtils.join(params, "\n"));
    }

    private String getTypeComment(Class clazz, Field field, String fileName) {
        if (field != null) clazz = field.getType();
        if (clazz.isPrimitive()) {
            return clazz.getSimpleName();
        } else if (clazz.getName().equals("java.lang.String")) {
            return "string";
        } else if (clazz.getName().contains("com.jhl")) {
            try {
                return generateDto(clazz, fileName);
            } catch (Exception e) {
                return "{}";
            }
        } else if (clazz.getName().contains("java.util") && clazz.getName().contains("List")) {
            try {
                ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                Class<?> genericArgument = (Class<?>) stringListType.getActualTypeArguments()[0];

                return getTypeComment(genericArgument, null, fileName) + "[]";
            } catch (Exception e) {
                return "*";
            }
        }
        return "*";
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
