package com.jhl.mds.jsclientgenerator;

import com.jhl.mds.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TypeCommentGenerator {

    private JsJsDTOClassGenerator jsDTOClassGenerator;
    private TemplateReader templateReader;

    public TypeCommentGenerator(TemplateReader templateReader) {
        this.templateReader = templateReader;
    }

    public void setJsDTOClassGenerator(JsJsDTOClassGenerator jsDTOClassGenerator) {
        this.jsDTOClassGenerator = jsDTOClassGenerator;
    }

    public String getFieldTypeComment(Field field) {
        try {
            return getTypeComment(field.getType(), (ParameterizedType) field.getGenericType());
        } catch (ClassCastException e) {
            return getTypeComment(field.getType(), null);
        }
    }

    public String getReturnTypeComment(Method method) {
        try {
            return getTypeComment(method.getReturnType(), (ParameterizedType) method.getGenericReturnType());
        } catch (ClassCastException e) {
            return getTypeComment(method.getReturnType(), null);
        }
    }

    public String getParameterTypeComment(Parameter parameter) {
        return getTypeComment(parameter.getType(), null);
    }

    private String getTypeComment(Class clazz, ParameterizedType parameterizedType) {
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) {
                return "boolean";
            } else if (clazz == char.class) {
                return "string";
            }
            return "number";
        } else if (clazz.getName().equals("java.lang.String")) {
            return "string";
        } else if (clazz.isArray()) {
            return getTypeComment(clazz.getComponentType(), null) + "[]";
        } else if (clazz == ApiResponse.class) {
            try {
                ParameterizedType genericArgument = (ParameterizedType) parameterizedType.getActualTypeArguments()[0];

                return getTypeComment((Class) genericArgument.getRawType(), genericArgument);
            } catch (ClassCastException e) {
                Class genericArgument = (Class) parameterizedType.getActualTypeArguments()[0];

                return getTypeComment(genericArgument, null);
            }
        } else if (clazz.getName().contains("com.jhl")) {
            try {
                return jsDTOClassGenerator.generateDto(clazz);
            } catch (Exception e) {
                return "{}";
            }
        } else if (List.class.isAssignableFrom(clazz)) {
            try {
                Class<?> genericArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];

                return getTypeComment(genericArgument, null) + "[]";
            } catch (Exception e) {
                return "*";
            }
        } else if (clazz.getName().contains("java.lang")) {
            if (Number.class.isAssignableFrom(clazz)) {
                return "number";
            } else if (clazz == Character.class) {
                return "string";
            }
            return clazz.getSimpleName().toLowerCase();
        }
        return "*";
    }

    public String renderMethodComment(Field[] fields) {
        List<String> params = new ArrayList<>();
        for (Field field : fields) {
            String renderedParam = templateReader.getMethodCommentTemplateParam().replaceAll("\\{param}", field.getName());
            try {
                renderedParam = renderedParam.replaceAll("\\{type}", getTypeComment(field.getType(), (ParameterizedType) field.getGenericType()));
            } catch (ClassCastException e) {
                renderedParam = renderedParam.replaceAll("\\{type}", getTypeComment(field.getType(), null));
            }

            params.add(renderedParam);
        }

        return templateReader.getMethodCommentTemplate().replaceAll("\\{params}", StringUtils.join(params, "\n"));
    }

    public String renderMethodComment(Parameter[] parameters, String[] parameterNames) {
        Map<String, String> nameMap = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            nameMap.put("arg" + i, parameterNames[i]);
        }

        List<String> params = new ArrayList<>();
        for (Parameter field : parameters) {
            if (field.getAnnotations().length > 0) {
                String paramName = nameMap.get(field.getName()) == null ? field.getName() : nameMap.get(field.getName());
                String renderedParam = templateReader.getMethodCommentTemplateParam().replaceAll("\\{param}", paramName);
                renderedParam = renderedParam.replaceAll("\\{type}", getTypeComment(field.getType(), null));

                params.add(renderedParam);
            }
        }

        return templateReader.getMethodCommentTemplate().replaceAll("\\{params}", StringUtils.join(params, "\n"));
    }
}
