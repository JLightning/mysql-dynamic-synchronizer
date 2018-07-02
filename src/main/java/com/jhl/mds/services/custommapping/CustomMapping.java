package com.jhl.mds.services.custommapping;

import com.jhl.mds.util.Regex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;
import java.util.Map;

@Service
public class CustomMapping {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public String resolve(String input, Map<String, Object> data) throws ScriptException {
        // replace string with marker
        List<String> list = Regex.findAllStringMatches(input, "'[^']+'");
        for (int i = 0; i < list.size(); i++) {
            input = input.replaceAll(list.get(i), "\\$" + i);
        }
        for (Map.Entry<String, Object> e : data.entrySet()) {
            Object value = e.getValue();
            if (value instanceof Number) {
                input = input.replaceAll(e.getKey(), String.valueOf(e.getValue()));
            } else {
                input = input.replaceAll(e.getKey(), "'" + String.valueOf(e.getValue()) + "'");
            }
        }
        for (int i = 0; i < list.size(); i++) {
            input = input.replaceAll("\\$" + i, list.get(i));
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");

        logger.info("try to evaluation " + input);
        Object result = engine.eval(input);
        return String.valueOf(result);
    }
}
