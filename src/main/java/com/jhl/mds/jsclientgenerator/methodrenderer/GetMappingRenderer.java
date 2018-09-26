package com.jhl.mds.jsclientgenerator.methodrenderer;

import com.jhl.mds.jsclientgenerator.TemplateReader;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GetMappingRenderer extends MethodRenderer {

    private final ParameterNameDiscoverer parameterNameDiscoverer;

    @Autowired
    public GetMappingRenderer(TemplateReader templateReader, ParameterNameDiscoverer parameterNameDiscoverer) {
        super(templateReader);
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Override
    public List<String> renderMethod(String baseUri, Method method, Annotation annotation) {
        if (!validate(annotation)) return null;
        List<String> result = new ArrayList<>();

        String methodAction = "get";
        String methodUri = baseUri + parseRequestAnnotation(annotation);

        List<String> methodParameters = new ArrayList<>();
        List<String> httpParameters = new ArrayList<>();
        int count = 0;
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        for (Parameter parameter : method.getParameters()) {
            Annotation[] annotations = parameter.getAnnotations();
            for (Annotation a : annotations) {
                String parameterName = parameterNames[count];
                if (a instanceof RequestParam) {
                    methodParameters.add(parameterName);
                    httpParameters.add(parameterName);
                    break;
                } else if (a instanceof PathVariable) {
                    methodUri = methodUri.replaceAll("\\{" + parameterName + "}", "' + " + parameterName + " + '");
                    methodParameters.add(parameterName);
                }
            }
            count++;
        }

        String renderMethodContent = renderMethod(method, methodAction, methodUri, methodParameters, httpParameters, null);
        result.add(renderMethodContent);

        return result;
    }

    private boolean validate(Annotation annotation) {
        return annotation instanceof GetMapping || (annotation instanceof RequestMapping && ArrayUtils.contains(((RequestMapping) annotation).value(), RequestMethod.GET));
    }
}
