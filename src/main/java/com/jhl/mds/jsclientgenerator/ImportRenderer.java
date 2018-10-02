package com.jhl.mds.jsclientgenerator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportRenderer {

    private JsClassImportRegistry jsClassImportRegistry;

    public ImportRenderer(JsClassImportRegistry jsClassImportRegistry) {
        this.jsClassImportRegistry = jsClassImportRegistry;
    }

    public List<String> renderImportForClass(Class clazz, String renderToFilename) {
        List<String> importLines = new ArrayList<>();

        List<JsClassImportRegistry.GeneratedDefinition> importDefs = jsClassImportRegistry.getImportMapForClass(clazz);
        if (importDefs != null) {
            Map<String, List<String>> importFromMap = new HashMap<>();
            for (JsClassImportRegistry.GeneratedDefinition def : importDefs) {
                String relativePath = resolveImportPath(renderToFilename, def.getFileName());
                if (relativePath.equals("./")) continue;
                if (!importFromMap.containsKey(relativePath)) importFromMap.put(relativePath, new ArrayList<>());
                importFromMap.get(relativePath).add(def.getClassName());
            }


            for (Map.Entry<String, List<String>> e : importFromMap.entrySet()) {
                List<String> list = e.getValue();
                for (int i = 0; i < list.size(); i += 4) {
                    importLines.add("import {" + StringUtils.join(list.subList(i, Math.min(i + 4, list.size())), ", ") + "} from '" + e.getKey() + "';");
                }
            }
        }

        return importLines;
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
        if (!result.contains("../")) result = "./" + result;
        return result;
    }
}
