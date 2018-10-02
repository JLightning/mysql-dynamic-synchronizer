package com.jhl.mds.jsclientgenerator.methodrenderer;

import com.jhl.mds.jsclientgenerator.JsClientGenerator;
import com.jhl.mds.jsclientgenerator.TemplateReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PutMappingRenderer extends MethodRenderer {

    private ParameterNameDiscoverer parameterNameDiscoverer;

    public PutMappingRenderer(TemplateReader templateReader, ParameterNameDiscoverer parameterNameDiscoverer) {
        super(templateReader);
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    @Override
    public List<String> renderMethod(String baseUri, Method method, Annotation annotation) {
        if (!validate(annotation)) return null;
        List<String> result = new ArrayList<>();

        String methodAction = "put";
        String methodUri = baseUri + parseRequestAnnotation(annotation);

        List<String> methodParameters = new ArrayList<>();
        List<String> httpParameters = new ArrayList<>();
        int count = 0;
        Parameter requestBodyParameter = null;
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        for (Parameter parameter : method.getParameters()) {
            Annotation[] annotations = parameter.getAnnotations();
            for (Annotation a : annotations) {
                String parameterName = parameterNames[count];
                if (a instanceof RequestParam) {
                    methodParameters.add(getMethodParametter(parameterName, parameter));
                    httpParameters.add(parameterName);
                    break;
                } else if (a instanceof RequestBody) {
                    requestBodyParameter = parameter;
                    methodAction = "putJson";
                    methodParameters.add(getMethodParametter(parameterName, parameter));
                    httpParameters.add(parameterName);
                    break;
                } else if (a instanceof PathVariable) {
                    methodUri = methodUri.replaceAll("\\{" + parameterName + "}", "' + " + parameterName + " + '");
                    methodParameters.add(getMethodParametter(parameterName, parameter));
                }
            }
            count++;
        }

        if (requestBodyParameter != null) {
            result.add(renderMethodWithRequestBody(method, methodAction, methodUri, requestBodyParameter));
        }

        String renderMethodContent = renderMethod(method, methodAction, methodUri, methodParameters, httpParameters, requestBodyParameter);
        result.add(renderMethodContent);

        return result;
    }

    private boolean validate(Annotation annotation) {
        return annotation instanceof PutMapping || (annotation instanceof RequestMapping && ArrayUtils.contains(((RequestMapping) annotation).value(), RequestMethod.PUT));
    }
}
