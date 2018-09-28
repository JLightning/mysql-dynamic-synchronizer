package com.jhl.mds.jsclientgenerator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TypeCommentGenerator {

    private JsDTOGenerator jsDTOGenerator;
    private TemplateReader templateReader;

    public TypeCommentGenerator(TemplateReader templateReader) {
        this.templateReader = templateReader;
    }

    public void setJsDTOGenerator(JsDTOGenerator jsDTOGenerator) {
        this.jsDTOGenerator = jsDTOGenerator;
    }

    public String getTypeComment(Class clazz, Field field, String fileName) {
        if (field != null) clazz = field.getType();
        if (clazz.isPrimitive()) {
            return clazz.getSimpleName();
        } else if (clazz.getName().equals("java.lang.String")) {
            return "string";
        } else if (clazz.getName().contains("com.jhl")) {
            try {
                return jsDTOGenerator.generateDto(clazz, fileName);
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

    public String renderMethodComment(Field[] fields, String fileName) {
        List<String> params = new ArrayList<>();
        for (Field field : fields) {
            String renderedParam = templateReader.getMethodCommentTemplateParam().replaceAll("\\{param}", field.getName());
            renderedParam = renderedParam.replaceAll("\\{type}", getTypeComment(field.getType(), field, fileName));

            params.add(renderedParam);
        }

        return templateReader.getMethodCommentTemplate().replaceAll("\\{params}", StringUtils.join(params, "\n"));
    }

    public String renderMethodComment(Parameter[] parameters, String[] parameterNames, String fileName) {
        Map<String, String> nameMap = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            nameMap.put("arg" + i, parameterNames[i]);
        }

        List<String> params = new ArrayList<>();
        for (Parameter field : parameters) {
            String renderedParam = templateReader.getMethodCommentTemplateParam().replaceAll("\\{param}", nameMap.get(field.getName()));
            renderedParam = renderedParam.replaceAll("\\{type}", getTypeComment(field.getType(), null, fileName));

            params.add(renderedParam);
        }

        return templateReader.getMethodCommentTemplate().replaceAll("\\{params}", StringUtils.join(params, "\n"));
    }
}
