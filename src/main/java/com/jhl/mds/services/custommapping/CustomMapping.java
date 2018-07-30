package com.jhl.mds.services.custommapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

public class CustomMapping {

    private final ScriptEngine jsEngine;
    private final Bindings bindings;
    private final ObjectMapper objectMapper;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public CustomMapping() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.jsEngine = manager.getEngineByName("nashorn");
        this.bindings = jsEngine.createBindings();
        this.objectMapper = new ObjectMapper();
    }

    public synchronized String resolve(String input, Map<String, Object> data) throws ScriptException, JsonProcessingException {
        bindings.clear();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            bindings.put(e.getKey(), e.getValue());
        }
        input = "var _row = " + objectMapper.writeValueAsString(data) + ";" + input;
        input = "var json = JSON.stringify;" + input;

//        logger.info("Try to evaluation " + input);
        Object result = jsEngine.eval(input, bindings);
        return String.valueOf(result);
    }
}
