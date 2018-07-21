package com.jhl.mds.jsclientgenerator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SpringBootApplication
public class JsClientGenerator {

    private static final String BASE_CLIENT_JS_DIRECTORY = "./src/main/resources/static/resources/js/source/api-client/";
    private static final String TEMPLATE_FILE = "static/resources/js/source/client.js.template";
    @Autowired
    private ParameterNameDiscoverer parameterNameDiscoverer;
    private String classTemplate = "";
    private String methodTemplate = "";

    @Bean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }

    @PostConstruct
    public void init() throws Exception {
        readTemplate();
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(JsClientController.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.jhl.mds")) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());

            JsClientController jsClientController = clazz.getAnnotation(JsClientController.class);
            RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);

            String baseUri = "";
            if (requestMapping != null) {
                baseUri = requestMapping.value()[0];
            }

            List<Method> methods = Arrays.asList(clazz.getMethods());
            methods.sort(Comparator.comparing(Method::getName));

            List<String> jsMethods = new ArrayList<>();

            for (Method method : methods) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation instanceof GetMapping || annotation instanceof PostMapping || annotation instanceof RequestMapping || annotation instanceof SubscribeMapping) {
                        jsMethods.addAll(renderMethod(baseUri, method, annotation));
                    }
                }
            }

            String renderedClass = renderClass(jsClientController, jsMethods);

            FileWriter fileWriter = new FileWriter(BASE_CLIENT_JS_DIRECTORY + jsClientController.fileName() + ".js");
            fileWriter.write(renderedClass);

            fileWriter.close();
        }
    }

    private List<String> renderMethod(String baseUri, Method method, Annotation annotation) {
        List<String> result = new ArrayList<>();

        String methodAction = "";
        String methodUri = baseUri;
        if (annotation instanceof GetMapping) {
            methodUri += ((GetMapping) annotation).value()[0];
            methodAction = "get";
        } else if (annotation instanceof PostMapping) {
            methodAction = "post";
            methodUri += ((PostMapping) annotation).value()[0];
        } else if (annotation instanceof RequestMapping) {
            if (Arrays.asList(((RequestMapping) annotation).method()).contains(RequestMethod.GET)) {
                methodAction = "get";
            } else if (Arrays.asList(((RequestMapping) annotation).method()).contains(RequestMethod.POST)) {
                methodAction = "post";
            }
            methodUri += ((RequestMapping) annotation).value()[0];
        } else if (annotation instanceof SubscribeMapping) {
            methodAction = "subscribe";
            methodUri = "/app" + ((SubscribeMapping) annotation).value()[0];
        }

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
                    methodParameters.add(parameterName);
                    httpParameters.add(parameterName);
                    break;
                } else if (a instanceof RequestBody) {
                    requestBodyParameter = parameter;
                    methodAction = "postJson";
                    methodParameters.add(parameterName);
                    httpParameters.add(parameterName);
                    break;
                } else if (a instanceof PathVariable) {
                    methodUri = methodUri.replaceAll("\\{" + parameterName + "}", "' + " + parameterName + " + '");
                    methodParameters.add(parameterName);
                } else if (a instanceof DestinationVariable) {
                    methodUri = methodUri.replaceAll("\\{" + parameterName + "}", "' + " + parameterName + " + '");
                    methodParameters.add(parameterName);
                    httpParameters.add("callback");
                }
            }
            count++;
        }

        if (requestBodyParameter != null) {
            String renderMethodContent = methodTemplate.replaceAll("\\{methodName}", method.getName() + "Flat");
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

        String renderMethodContent = methodTemplate.replaceAll("\\{methodName}", method.getName());
        renderMethodContent = renderMethodContent.replaceAll("\\{methodAction}", methodAction);
        renderMethodContent = renderMethodContent.replaceAll("\\{methodUri}", "'" + methodUri + "'");
        if (methodAction.equals("postJson")) {
            renderMethodContent = renderMethodContent.replaceAll("\\{methodParameters}", StringUtils.join(methodParameters, ", "));
            renderMethodContent = renderMethodContent.replaceAll("\\{\\{httpParameters}}", StringUtils.join(httpParameters, ", "));
        } else if (methodAction.equals("subscribe")) {
            methodParameters.add("callback");
            renderMethodContent = renderMethodContent.replaceAll("\\{methodParameters}", StringUtils.join(methodParameters, ", "));
            renderMethodContent = renderMethodContent.replaceAll("\\{\\{httpParameters}}", "callback");
        } else {
            renderMethodContent = renderMethodContent.replaceAll("\\{methodParameters}", StringUtils.join(methodParameters, ", "));
            renderMethodContent = renderMethodContent.replaceAll("\\{httpParameters}", StringUtils.join(httpParameters, ", "));
        }

        result.add(renderMethodContent);

        return result;
    }

    private String renderClass(JsClientController jsClientController, List<String> jsMethods) {
        String renderClassContent = classTemplate.replaceAll("\\{className}", jsClientController.className());
        renderClassContent = renderClassContent.replaceAll("\\{methods}", StringUtils.join(jsMethods, "\n\n"));
        renderClassContent = renderClassContent.replaceAll("\\{newVariableClassName}", StringUtils.uncapitalize(jsClientController.className()));

        return renderClassContent;
    }

    private void readTemplate() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File templateFile = new File(classLoader.getResource(TEMPLATE_FILE).getFile());

        StringBuilder classTemplateBuilder = new StringBuilder();
        StringBuilder methodTemplateBuilder = new StringBuilder();
        boolean isClassTemplate = false;
        BufferedReader br = new BufferedReader(new FileReader(templateFile));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.equals("[class_template]")) {
                isClassTemplate = true;
                continue;
            } else if (line.equals("[method_template]")) {
                isClassTemplate = false;
                continue;
            }
            if (isClassTemplate) {
                if (classTemplateBuilder.length() > 0) classTemplateBuilder.append("\n");
                classTemplateBuilder.append(line);
            } else {
                if (methodTemplateBuilder.length() > 0) methodTemplateBuilder.append("\n");
                methodTemplateBuilder.append(line);
            }
        }
        classTemplate = classTemplateBuilder.toString();
        methodTemplate = methodTemplateBuilder.toString();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(JsClientGenerator.class);
        ctx.close();
    }
}
