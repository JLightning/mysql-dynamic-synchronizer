package com.jhl.mds.jsclientgenerator;

import com.jhl.mds.jsclientgenerator.methodrenderer.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SpringBootApplication
public class JsClientGenerator {

    private static final String BASE_CLIENT_JS_DIRECTORY = "./src/main/resources/static/resources/js/source/api-client/";
    private TemplateReader templateReader;
    private MethodRenderer[] methodRenderers;

    @Autowired
    public JsClientGenerator(
            TemplateReader templateReader,
            GetMappingRenderer getMappingRenderer,
            PostMappingRenderer postMappingRenderer,
            PutMappingRenderer putMappingRenderer,
            DeleteMappingRenderer deleteMappingRenderer,
            SubscribeMappingRenderer subscribeMappingRenderer
    ) {
        this.templateReader = templateReader;
        methodRenderers = new MethodRenderer[]{getMappingRenderer, postMappingRenderer, putMappingRenderer, deleteMappingRenderer, subscribeMappingRenderer};
    }

    @PostConstruct
    public void init() throws Exception {
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
                    if (annotation instanceof GetMapping || annotation instanceof PostMapping || annotation instanceof RequestMapping || annotation instanceof SubscribeMapping
                            || annotation instanceof DeleteMapping || annotation instanceof PutMapping) {
                        List<String> renderedMethods = renderMethod(baseUri, method, annotation);
                        if (renderedMethods != null) jsMethods.addAll(renderedMethods);
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
        for (MethodRenderer methodRenderer : methodRenderers) {
            try {
                List<String> rendered = methodRenderer.renderMethod(baseUri, method, annotation);
                if (rendered != null) result.addAll(rendered);
            } catch (Exception e) {

            }
        }
        return result;
    }

    private String renderClass(JsClientController jsClientController, List<String> jsMethods) {
        String renderClassContent = templateReader.getClassTemplate().replaceAll("\\{className}", jsClientController.className());
        renderClassContent = renderClassContent.replaceAll("\\{methods}", StringUtils.join(jsMethods, "\n\n"));
        renderClassContent = renderClassContent.replaceAll("\\{newVariableClassName}", StringUtils.uncapitalize(jsClientController.className()));

        return renderClassContent;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(JsClientGenerator.class);
        ctx.close();
    }
}
