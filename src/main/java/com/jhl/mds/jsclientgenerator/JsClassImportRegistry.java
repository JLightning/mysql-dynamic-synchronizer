package com.jhl.mds.jsclientgenerator;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JsClassImportRegistry {

    private Map<Class, List<GeneratedDefinition>> tmpGenerated = new HashMap<>();
    @Getter
    private Map<Class, GeneratedDefinition> generated = new HashMap<>();
    private List<Class> currentGenerateForList = new ArrayList<>();

    public void addImportMap(GeneratedDefinition def) {
        if (currentGenerateForList.size() == 0) return;
        Class currentGenerateFor = currentGenerateForList.get(currentGenerateForList.size() - 1);
        if (!tmpGenerated.containsKey(currentGenerateFor)) {
            tmpGenerated.put(currentGenerateFor, new ArrayList<>());
        }
        List<GeneratedDefinition> list = tmpGenerated.get(currentGenerateFor);
        if (!list.contains(def)) list.add(def);
    }

    public List<GeneratedDefinition> getImportMapForClass(Class cls) {
        return tmpGenerated.get(cls);
    }

    public void addGeneratedClass(Class cls, GeneratedDefinition def) {
        generated.put(cls, def);
    }

    public void setCurrentGenerateFor(Class cls) {
        currentGenerateForList.add(cls);
    }

    public void doneFor(Class cls) {
        if (currentGenerateForList.size() > 0 && currentGenerateForList.get(currentGenerateForList.size() - 1) == cls) {
            currentGenerateForList = currentGenerateForList.subList(0, currentGenerateForList.size() - 1);
        }
    }

    @Data
    @EqualsAndHashCode
    public static class GeneratedDefinition {
        private String className;
        private String fileName;

        public GeneratedDefinition(String className, String fileName) {
            this.className = className;
            this.fileName = fileName;
        }
    }
}
