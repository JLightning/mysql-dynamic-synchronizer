package com.jhl.mds.jsclientgenerator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.util.List;

public abstract class JsDTOGenerator {

    static final String BASE_CLIENT_JS_DIRECTORY = "./src/main/resources/static/resources/js/source/dto/";
    private TemplateReader templateReader;

    public JsDTOGenerator(TemplateReader templateReader) {
        this.templateReader = templateReader;
    }

    String renderClass(String className, List<String> fields, List<String> constructorParameters, List<String> constructorSetters, String constructorComment, String methods) {
        String renderClassContent = templateReader.getDtoClassTemplate().replaceAll("\\{className}", className);
        renderClassContent = renderClassContent.replaceAll("\\{fields}", StringUtils.join(fields, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_parameters}", StringUtils.join(constructorParameters, ", "));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_setters}", StringUtils.join(constructorSetters, "\n"));
        renderClassContent = renderClassContent.replaceAll("\\{constructor_comment}", constructorComment);
        renderClassContent = renderClassContent.replaceAll("\\{methods}", methods);

        return renderClassContent;
    }

    Pair<String, String> getFileNameAndClassName(JsClientDTO jsClientDTO, Class<?> clazz) {
        if (jsClientDTO != null) {
            return Pair.of(jsClientDTO.fileName(), jsClientDTO.className());
        } else {
            return Pair.of(classToFileName(clazz), clazz.getSimpleName());
        }
    }

    private String classToFileName(Class<?> clazz) {
        String simpleName = clazz.getSimpleName();
        String newName = "";
        char lastChar = 97;
        for (int i = 0; i < simpleName.length(); i++) {
            char c = simpleName.charAt(i);
            if (i > 0 && c < 97 && lastChar >= 97) newName += "-" + c;
            else newName += c;
            lastChar = c;
        }
        return newName.toLowerCase();
    }
}
