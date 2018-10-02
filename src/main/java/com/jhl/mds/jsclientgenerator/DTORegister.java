package com.jhl.mds.jsclientgenerator;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DTORegister {

    private List<GeneratedDefinition> tmpGenerated = new ArrayList<>();

    public void addTmpGenerated(GeneratedDefinition def) {
        tmpGenerated.add(def);
    }

    public List<GeneratedDefinition> getTmpGenerated() {
        List<GeneratedDefinition> tmp = new ArrayList<>();
        tmp.addAll(tmpGenerated);
//        tmpGenerated.clear();
        return tmp;
    }

    @Data
    public static class GeneratedDefinition {
        private String className;
        private String fileName;

        public GeneratedDefinition(String className, String fileName) {
            this.className = className;
            this.fileName = fileName;
        }
    }
}
