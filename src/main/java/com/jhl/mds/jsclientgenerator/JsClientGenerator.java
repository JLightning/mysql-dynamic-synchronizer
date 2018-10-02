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
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@SpringBootApplication
public class JsClientGenerator {

    private static final String BASE_CLIENT_JS_DIRECTORY = "./src/main/resources/static/resources/js/source/api-client/";
    private TemplateReader templateReader;
    private FileUtils fileUtils;
    private MethodRenderer[] methodRenderers;
    private DTORegistry dtoRegistry;

    @Autowired
    public JsClientGenerator(
            JsDTOGenerator jsDTOGenerator,
            DTORegistry dtoRegistry,
            TemplateReader templateReader,
            GetMappingRenderer getMappingRenderer,
            PostMappingRenderer postMappingRenderer,
            PutMappingRenderer putMappingRenderer,
            DeleteMappingRenderer deleteMappingRenderer,
            SubscribeMappingRenderer subscribeMappingRenderer,
            FileUtils fileUtils
    ) {
        this.dtoRegistry = dtoRegistry;
        this.templateReader = templateReader;
        this.fileUtils = fileUtils;
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
                    List<String> renderedMethods = renderMethod(baseUri, method, annotation);
                    if (renderedMethods != null) jsMethods.addAll(renderedMethods);
                }
            }

            String renderedClass = renderClass(jsClientController, jsMethods, BASE_CLIENT_JS_DIRECTORY + jsClientController.fileName() + ".js");

            fileUtils.initClean(BASE_CLIENT_JS_DIRECTORY + jsClientController.fileName() + ".js");
            fileUtils.append(BASE_CLIENT_JS_DIRECTORY + jsClientController.fileName() + ".js", renderedClass);
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

    private String renderClass(JsClientController jsClientController, List<String> jsMethods, String renderToFilename) {
        String renderClassContent = templateReader.getClassTemplate().replaceAll("\\{className}", jsClientController.className());
        renderClassContent = renderClassContent.replaceAll("\\{methods}", StringUtils.join(jsMethods, "\n\n"));
        renderClassContent = renderClassContent.replaceAll("\\{newVariableClassName}", StringUtils.uncapitalize(jsClientController.className()));

        List<DTORegistry.GeneratedDefinition> importDefs = dtoRegistry.getTmpGenerated();
        Map<String, List<String>> importFromMap = new HashMap<>();
        for (DTORegistry.GeneratedDefinition def : importDefs) {
            String relativePath = resolveImportPath(renderToFilename, def.getFileName());
            if (!importFromMap.containsKey(relativePath)) importFromMap.put(relativePath, new ArrayList<>());
            importFromMap.get(relativePath).add(def.getClassName());
        }

        List<String> importLines = new ArrayList<>();

        for (Map.Entry<String, List<String>> e : importFromMap.entrySet()) {
            importLines.add("import {" + StringUtils.join(e.getValue(), ", ") + "} from '" + e.getKey() + "';");
        }

        renderClassContent = renderClassContent.replaceAll("\\{imports}", StringUtils.join(importLines, "\n"));

        return renderClassContent;
    }

    private String resolveImportPath(String classFileName, String dtoFileName) {
        String[] classFileNames = classFileName.split("/");
        String[] dtoFileNames = dtoFileName.split("/");
        String result = "";
        for (int i = 0; i < classFileNames.length - 1; i++) {
            if (classFileNames[i].equals(dtoFileNames[i])) continue;
            result += "../";
        }

        for (int i = 0; i < dtoFileNames.length; i++) {
            if (classFileNames[i].equals(dtoFileNames[i])) continue;
            result += dtoFileNames[i] + "/";
        }
        result = result.replaceAll("\\.js/$", "");
        return result;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(JsClientGenerator.class);
        ctx.close();
    }
}
