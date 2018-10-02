package com.jhl.mds.jsclientgenerator;

import com.jhl.mds.jsclientgenerator.methodrenderer.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
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
    private FileCleaner fileCleaner;
    private MethodRenderer[] methodRenderers;
    private DTORegister dtoRegister;

    @Autowired
    public JsClientGenerator(
            JsDTOGenerator jsDTOGenerator,
            DTORegister dtoRegister,
            TemplateReader templateReader,
            GetMappingRenderer getMappingRenderer,
            PostMappingRenderer postMappingRenderer,
            PutMappingRenderer putMappingRenderer,
            DeleteMappingRenderer deleteMappingRenderer,
            SubscribeMappingRenderer subscribeMappingRenderer,
            FileCleaner fileCleaner
    ) {
        this.dtoRegister = dtoRegister;
        this.templateReader = templateReader;
        this.fileCleaner = fileCleaner;
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

            String renderedClass = renderClass(jsClientController, jsMethods);

            fileCleaner.clean(BASE_CLIENT_JS_DIRECTORY + jsClientController.fileName() + ".js");

            FileWriter fileWriter = new FileWriter(BASE_CLIENT_JS_DIRECTORY + jsClientController.fileName() + ".js", true);
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

    // TODO: fix import from path
    private String renderClass(JsClientController jsClientController, List<String> jsMethods) {
        String renderClassContent = templateReader.getClassTemplate().replaceAll("\\{className}", jsClientController.className());
        renderClassContent = renderClassContent.replaceAll("\\{methods}", StringUtils.join(jsMethods, "\n\n"));
        renderClassContent = renderClassContent.replaceAll("\\{newVariableClassName}", StringUtils.uncapitalize(jsClientController.className()));

        List<DTORegister.GeneratedDefinition> importDefs = dtoRegister.getTmpGenerated();
        List<String> classToImport = new ArrayList<>();
        for (DTORegister.GeneratedDefinition def : importDefs) {
            classToImport.add(def.getClassName());
        }

        renderClassContent = renderClassContent.replaceAll("\\{imports}", "import {" + StringUtils.join(classToImport, ", ") + "} from '../dto/common';");

        return renderClassContent;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(JsClientGenerator.class);
        ctx.close();
    }
}
