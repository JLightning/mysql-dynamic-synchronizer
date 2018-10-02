package com.jhl.mds.jsclientgenerator.methodrenderer;

import com.jhl.mds.jsclientgenerator.TemplateReader;
import com.jhl.mds.jsclientgenerator.TypeCommentGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubscribeMappingRenderer extends MethodRenderer {

    private TemplateReader templateReader;
    private ParameterNameDiscoverer parameterNameDiscoverer;
    private TypeCommentGenerator typeCommentGenerator;

    public SubscribeMappingRenderer(TemplateReader templateReader, ParameterNameDiscoverer parameterNameDiscoverer, TypeCommentGenerator typeCommentGenerator) {
        super(templateReader);
        this.templateReader = templateReader;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        this.typeCommentGenerator = typeCommentGenerator;
    }

    @Override
    public List<String> renderMethod(String baseUri, Method method, Annotation annotation) {
        if (!validate(annotation)) return null;
        List<String> result = new ArrayList<>();

        String methodAction = "subscribe";
        String methodUri = "/app" + parseRequestAnnotation(annotation);
        List<String> methodParameters = new ArrayList<>();
        int count = 0;
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        for (Parameter parameter : method.getParameters()) {
            Annotation[] annotations = parameter.getAnnotations();
            for (Annotation a : annotations) {
                String parameterName = parameterNames[count];
                if (a instanceof DestinationVariable) {
                    methodUri = methodUri.replaceAll("\\{" + parameterName + "}", "' + " + parameterName + " + '");
                    methodParameters.add(getMethodParametter(parameterName, parameter));
                }
            }
            count++;
        }

        String returnType = typeCommentGenerator.getReturnTypeComment(method);

        String renderMethodContent = templateReader.getMethodTemplate().replaceAll("\\{methodName}", method.getName());
        renderMethodContent = renderMethodContent.replaceAll("\\{methodAction}", methodAction);
        renderMethodContent = renderMethodContent.replaceAll("\\{methodUri}", "'" + methodUri + "'");
        methodParameters.add("callback: (" + returnType+") => any");
        renderMethodContent = renderMethodContent.replaceAll("\\{methodParameters}", StringUtils.join(methodParameters, ", "));
        renderMethodContent = renderMethodContent.replaceAll("\\{\\{httpParameters}}", "callback");
        renderMethodContent = renderMethodContent.replaceAll("\\{return_type}", "void");

        result.add(renderMethodContent);

        return result;
    }

    private boolean validate(Annotation annotation) {
        return annotation instanceof SubscribeMapping;
    }
}
