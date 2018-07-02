package com.jhl.mds.services.custommapping;

import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

@Service
public class CustomMapping {

    public String resolve(String input, Map<String, Object> data) throws ScriptException {
        for (Map.Entry<String, Object> e : data.entrySet()) {
            input = input.replaceAll(e.getKey(), "'" + String.valueOf(e.getValue()) + "'");
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        Object result = engine.eval(input);
        return String.valueOf(result);
    }
}
