package com.jhl.mds.jsclientgenerator;

import lombok.Getter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

@Service
public class TemplateReader {

    private static final String TEMPLATE_FILE = "static/resources/js/source/client.js.template";
    @Getter
    private String classTemplate = "";
    @Getter
    private String methodTemplate = "";
    @Getter
    private String methodCommentTemplate = "";
    @Getter
    private String methodCommentTemplateParam = "";
    @Getter
    private String dtoClassTemplate = "";
    @Getter
    private String dtoFieldTemplate = "";
    @Getter
    private String dtoConstructorSetterTemplate = "";

    @PostConstruct
    public void readTemplate() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File templateFile = new File(classLoader.getResource(TEMPLATE_FILE).getFile());

        StringBuilder classTemplateBuilder = new StringBuilder();
        StringBuilder methodTemplateBuilder = new StringBuilder();
        StringBuilder methodCommentTemplateBuilder = new StringBuilder();
        StringBuilder methodCommentTemplateParamBuilder = new StringBuilder();
        StringBuilder dtoClassTemplateBuilder = new StringBuilder();
        StringBuilder dtoFieldTemplateBuilder = new StringBuilder();
        StringBuilder dtoConstructorSetterTemplateBuilder = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(templateFile));
        TemplateType templateType = null;
        String line;
        while ((line = br.readLine()) != null) {
            switch (line) {
                case "[class_template]":
                    templateType = TemplateType.CLIENT_CLASS_TEMPLATE;
                    continue;
                case "[method_template]":
                    templateType = TemplateType.CLIENT_METHOD_TEMPLATE;
                    continue;
                case "[method_comment_template]":
                    templateType = TemplateType.METHOD_COMMENT_TEMPLATE;
                    continue;
                case "[method_comment_template_param]":
                    templateType = TemplateType.METHOD_COMMENT_TEMPLATE_PARAM;
                    continue;
                case "[dto_class_template]":
                    templateType = TemplateType.DTO_CLASS_TEMPLATE;
                    continue;
                case "[dto_field_template]":
                    templateType = TemplateType.DTO_FIELD_TEMPLATE;
                    continue;
                case "[dto_constructor_setter_template]":
                    templateType = TemplateType.DTO_CONSTRUCTOR_SETTER_TEMPLATE;
                    continue;

            }
            if (templateType != null) {
                switch (templateType) {
                    case CLIENT_CLASS_TEMPLATE:
                        if (classTemplateBuilder.length() > 0) classTemplateBuilder.append("\n");
                        classTemplateBuilder.append(line);
                        break;
                    case CLIENT_METHOD_TEMPLATE:
                        if (methodTemplateBuilder.length() > 0) methodTemplateBuilder.append("\n");
                        methodTemplateBuilder.append(line);
                        break;
                    case METHOD_COMMENT_TEMPLATE:
                        if (methodCommentTemplateBuilder.length() > 0) methodCommentTemplateBuilder.append("\n");
                        methodCommentTemplateBuilder.append(line);
                        break;
                    case METHOD_COMMENT_TEMPLATE_PARAM:
                        if (methodCommentTemplateParamBuilder.length() > 0)
                            methodCommentTemplateParamBuilder.append("\n");
                        methodCommentTemplateParamBuilder.append(line);
                        break;
                    case DTO_CLASS_TEMPLATE:
                        if (dtoClassTemplateBuilder.length() > 0) dtoClassTemplateBuilder.append("\n");
                        dtoClassTemplateBuilder.append(line);
                        break;
                    case DTO_FIELD_TEMPLATE:
                        if (dtoFieldTemplateBuilder.length() > 0) dtoFieldTemplateBuilder.append("\n");
                        dtoFieldTemplateBuilder.append(line);
                        break;
                    case DTO_CONSTRUCTOR_SETTER_TEMPLATE:
                        if (dtoConstructorSetterTemplateBuilder.length() > 0)
                            dtoConstructorSetterTemplateBuilder.append("\n");
                        dtoConstructorSetterTemplateBuilder.append(line);
                        break;
                }
            }
        }
        classTemplate = classTemplateBuilder.toString();
        methodTemplate = methodTemplateBuilder.toString();
        methodCommentTemplate = methodCommentTemplateBuilder.toString();
        methodCommentTemplateParam = methodCommentTemplateParamBuilder.toString();
        dtoClassTemplate = dtoClassTemplateBuilder.toString();
        dtoFieldTemplate = dtoFieldTemplateBuilder.toString();
        dtoConstructorSetterTemplate = dtoConstructorSetterTemplateBuilder.toString();
    }

    private enum TemplateType {
        CLIENT_CLASS_TEMPLATE,
        CLIENT_METHOD_TEMPLATE,
        METHOD_COMMENT_TEMPLATE,
        METHOD_COMMENT_TEMPLATE_PARAM,
        DTO_CLASS_TEMPLATE,
        DTO_FIELD_TEMPLATE,
        DTO_CONSTRUCTOR_SETTER_TEMPLATE,
    }
}
