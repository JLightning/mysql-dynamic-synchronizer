package com.jhl.mds.services.custommapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;

@Service
public class CustomMapping {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public String resolve(String input, Map<String, Object> data) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");

        Bindings bindings = engine.createBindings();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            bindings.put(e.getKey(), e.getValue());
        }

        logger.info("Try to evaluation " + input);
        Object result = engine.eval(input, bindings);
        return String.valueOf(result);
    }
}
