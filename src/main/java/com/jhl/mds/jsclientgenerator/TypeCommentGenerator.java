package com.jhl.mds.jsclientgenerator;

import com.jhl.mds.dto.ApiResponse;
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

    public String getTypeComment(Class clazz, ParameterizedType parameterizedType, String fileName) {
        if (clazz.isPrimitive()) {
            return clazz.getSimpleName();
        } else if (clazz.getName().equals("java.lang.String")) {
            return "string";
        } else if (clazz.isArray()) {
            return getTypeComment(clazz.getComponentType(), null, fileName) + "[]";
        } else if (clazz == ApiResponse.class) {
            try {
                ParameterizedType genericArgument = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];

                return getTypeComment((Class) genericArgument.getRawType(), genericArgument, fileName);
            } catch (ClassCastException e) {
                Class genericArgument = (Class) parameterizedType.getActualTypeArguments()[0];

                return getTypeComment(genericArgument, null, fileName);
            }
        } else if (clazz.getName().contains("com.jhl")) {
            try {
                return jsDTOGenerator.generateDto(clazz, fileName);
            } catch (Exception e) {
                return "{}";
            }
        } else if (clazz.getName().contains("java.util") && clazz.getName().contains("List")) {
            try {
                Class<?> genericArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];

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
            try {
                renderedParam = renderedParam.replaceAll("\\{type}", getTypeComment(field.getType(), (ParameterizedType) field.getGenericType(), fileName));
            } catch (ClassCastException e) {
                renderedParam = renderedParam.replaceAll("\\{type}", getTypeComment(field.getType(), null, fileName));
            }

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
            if (field.getAnnotations().length > 0) {
                String renderedParam = templateReader.getMethodCommentTemplateParam().replaceAll("\\{param}", nameMap.get(field.getName()));
                renderedParam = renderedParam.replaceAll("\\{type}", getTypeComment(field.getType(), null, fileName));

                params.add(renderedParam);
            }
        }

        return templateReader.getMethodCommentTemplate().replaceAll("\\{params}", StringUtils.join(params, "\n"));
    }
}
