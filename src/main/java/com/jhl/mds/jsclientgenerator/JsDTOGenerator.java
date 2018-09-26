package com.jhl.mds.jsclientgenerator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Service
public class JsDTOGenerator {

    private static final String BASE_CLIENT_JS_DIRECTORY = "./src/main/resources/static/resources/js/source/dto/";
    private TemplateReader templateReader;

    public JsDTOGenerator(TemplateReader templateReader) {
        this.templateReader = templateReader;
    }

    @PostConstruct
    public void init() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(JsClientDTO.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.jhl.mds")) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());

            JsClientDTO jsClientDTO = clazz.getAnnotation(JsClientDTO.class);

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
                renderedField = renderedField.replaceAll("\\{type}", field.getType().getSimpleName());
                renderedField = renderedField.replaceAll("\\{default_value}", defaultValue);
                fieldStr.add(renderedField);

                constructorParameters.add(field.getName());

                constructorSetters.add(templateReader.getDtoConstructorSetterTemplate().replaceAll("\\{parameter}", field.getName()));
            }

            String renderedClass = renderClass(jsClientDTO, fieldStr, constructorParameters, constructorSetters, renderMethodComment(fields));

            FileWriter fileWriter = new FileWriter(BASE_CLIENT_JS_DIRECTORY + jsClientDTO.fileName() + ".js");
            fileWriter.write(renderedClass);

            fileWriter.close();
        }
    }

    private String renderMethodComment(Field[] fields) {
        List<String> params = new ArrayList<>();
        for (Field field : fields) {
            String renderedParam = templateReader.getMethodCommentTemplateParam().replaceAll("\\{param}", field.getName());
            renderedParam = renderedParam.replaceAll("\\{type}", field.getType().getSimpleName());

            params.add(renderedParam);
        }

        return templateReader.getMethodCommentTemplate().replaceAll("\\{params}", StringUtils.join(params, "\n"));
    }

    private String renderClass(JsClientDTO jsClientDTO, List<String> fields, List<String> constructorParameters, List<String> constructorSetters, String constructorComment) {
        String renderClassContent = templateReader.getDtoClassTemplate().replaceAll("\\{className}", jsClientDTO.className());
        renderClassContent = renderClassContent.replaceAll("\\{fields}", StringUtils.join(fields, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_parameters}", StringUtils.join(constructorParameters, ", "));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_setters}", StringUtils.join(constructorSetters, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_comment}", constructorComment);

        return renderClassContent;
    }
}
