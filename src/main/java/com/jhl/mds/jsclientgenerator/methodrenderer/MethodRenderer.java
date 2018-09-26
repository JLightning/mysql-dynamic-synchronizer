package com.jhl.mds.jsclientgenerator.methodrenderer;

import com.jhl.mds.jsclientgenerator.JsClientGenerator;
import com.jhl.mds.jsclientgenerator.TemplateReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public abstract class MethodRenderer {

    private TemplateReader templateReader;

    public MethodRenderer(TemplateReader templateReader) {
        this.templateReader = templateReader;
    }

    public abstract List<String> renderMethod(String baseUri, Method method, Annotation annotation);

    String parseRequestAnnotation(Annotation annotation) {
        if (annotation instanceof RequestMapping) {
            return ((RequestMapping) annotation).value()[0];
        } else if (annotation instanceof GetMapping) {
            return ((GetMapping) annotation).value()[0];
        } else if (annotation instanceof PostMapping) {
            return ((PostMapping) annotation).value()[0];
        } else if (annotation instanceof PutMapping) {
            return ((PutMapping) annotation).value()[0];
        } else if (annotation instanceof DeleteMapping) {
            return ((DeleteMapping) annotation).value()[0];
        } else if (annotation instanceof SubscribeMapping) {
            return ((SubscribeMapping) annotation).value()[0];
        }
        return "";
    }

    void renderMethodWithRequestBody(Method method, List<String> result, String methodAction, String methodUri, Parameter requestBodyParameter) {
        String renderMethodContent = templateReader.getMethodTemplate().replaceAll("\\{methodName}", method.getName() + "Flat");
        renderMethodContent = renderMethodContent.replaceAll("\\{methodAction}", methodAction);
        renderMethodContent = renderMethodContent.replaceAll("\\{methodUri}", "'" + methodUri + "'");

        Field[] fields = requestBodyParameter.getType().getDeclaredFields();
        List<String> fieldStr = new ArrayList<>();
        for (Field field : fields) {
            fieldStr.add(field.getName());
        }

        renderMethodContent = renderMethodContent.replaceAll("\\{methodParameters}", StringUtils.join(fieldStr, ", "));
        renderMethodContent = renderMethodContent.replaceAll("\\{httpParameters}", StringUtils.join(fieldStr, ", "));

        result.add(renderMethodContent);
    }

    String renderMethod(Method method, String methodAction, String methodUri, List<String> methodParameters, List<String> httpParameters, Parameter requestBodyParameter) {
        String renderMethodContent = templateReader.getMethodTemplate().replaceAll("\\{methodName}", method.getName());
        renderMethodContent = renderMethodContent.replaceAll("\\{methodAction}", methodAction);
        renderMethodContent = renderMethodContent.replaceAll("\\{methodUri}", "'" + methodUri + "'");
        if (requestBodyParameter != null) {
            renderMethodContent = renderMethodContent.replaceAll("\\{methodParameters}", StringUtils.join(methodParameters, ", "));
            renderMethodContent = renderMethodContent.replaceAll("\\{\\{httpParameters}}", StringUtils.join(httpParameters, ", "));
        } else {
            renderMethodContent = renderMethodContent.replaceAll("\\{methodParameters}", StringUtils.join(methodParameters, ", "));
            renderMethodContent = renderMethodContent.replaceAll("\\{httpParameters}", StringUtils.join(httpParameters, ", "));
        }
        return renderMethodContent;
    }
}
