package com.jhl.mds.jsclientgenerator;

import com.jhl.mds.jsclientgenerator.methodrenderer.*;
import com.jhl.mds.jsclientgenerator.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@SpringBootApplication
@Slf4j
public class JsClientGenerator {

    private static final String BASE_CLIENT_JS_DIRECTORY = "./src/main/resources/static/resources/js/source/api-client/";
    private TemplateReader templateReader;
    private ImportRenderer importRenderer;
    private FileUtils fileUtils;
    private MethodRenderer[] methodRenderers;
    private JsClassImportRegistry jsClassImportRegistry;

    @Autowired
    public JsClientGenerator(
            JsJsDTOClassGenerator jsDTOClassGenerator,
            JsClassImportRegistry jsClassImportRegistry,
            TemplateReader templateReader,
            GetMappingRenderer getMappingRenderer,
            PostMappingRenderer postMappingRenderer,
            PutMappingRenderer putMappingRenderer,
            DeleteMappingRenderer deleteMappingRenderer,
            SubscribeMappingRenderer subscribeMappingRenderer,
            ImportRenderer importRenderer,
            FileUtils fileUtils
    ) {
        this.jsClassImportRegistry = jsClassImportRegistry;
        this.templateReader = templateReader;
        this.importRenderer = importRenderer;
        this.fileUtils = fileUtils;
        methodRenderers = new MethodRenderer[]{getMappingRenderer, postMappingRenderer, putMappingRenderer, deleteMappingRenderer, subscribeMappingRenderer};
    }

    public void start() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(JsClientController.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("com.jhl.mds")) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());

            jsClassImportRegistry.setCurrentGenerateFor(clazz);

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
                    List<String> renderedMethods = renderMethod(baseUri, method, annotation);
                    if (renderedMethods != null) jsMethods.addAll(renderedMethods);
                }
            }

            String renderedClass = renderClass(clazz, jsClientController, jsMethods, BASE_CLIENT_JS_DIRECTORY + jsClientController.fileName() + ".js");

            fileUtils.initClean(BASE_CLIENT_JS_DIRECTORY + jsClientController.fileName() + ".js");
            fileUtils.append(BASE_CLIENT_JS_DIRECTORY + jsClientController.fileName() + ".js", renderedClass);

            jsClassImportRegistry.doneFor(clazz);
        }
    }

    private List<String> renderMethod(String baseUri, Method method, Annotation annotation) {
        List<String> result = new ArrayList<>();
        for (MethodRenderer methodRenderer : methodRenderers) {
            try {
                List<String> rendered = methodRenderer.renderMethod(baseUri, method, annotation);
                if (rendered != null) result.addAll(rendered);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String renderClass(Class clazz, JsClientController jsClientController, List<String> jsMethods, String renderToFilename) {
        String renderClassContent = templateReader.getClassTemplate().replaceAll("\\{className}", jsClientController.className());
        renderClassContent = renderClassContent.replaceAll("\\{methods}", StringUtils.join(jsMethods, "\n\n"));
        renderClassContent = renderClassContent.replaceAll("\\{newVariableClassName}", StringUtils.uncapitalize(jsClientController.className()));

        renderClassContent = renderClassContent.replaceAll("\\{imports}", StringUtils.join(importRenderer.renderImportForClass(clazz, renderToFilename), "\n"));

        return renderClassContent;
    }
}
