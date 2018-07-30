package com.jhl.mds.services.customefilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhl.mds.services.custommapping.CustomMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class CustomFilterPool {

    private static final int POOL_SIZE = 4;
    private ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
    private List<CustomMapping> customMappingList = new ArrayList<>();
    private int roundRobin = 0;
    private ObjectMapper objectMapper;

    @Autowired
    public CustomFilterPool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void init() {
        for (int i = 0; i < POOL_SIZE; i++) {
            customMappingList.add(new CustomMapping(objectMapper));
        }
    }

    public Future<Boolean> resolve(String input, Map<String, Object> data) {
        final int tmpRoundRobin;
        roundRobin++;
        if (roundRobin >= POOL_SIZE) roundRobin = 0;
        tmpRoundRobin = roundRobin % POOL_SIZE;
        return executor.submit(() -> {
            try {
                String str = customMappingList.get(tmpRoundRobin).resolve(input, data);
                if (str.equals("true")) {
                    return true;
                } else if (str.equals("false")) {
                    return false;
                }
                throw new RuntimeException("Filter invalid");
            } catch (ScriptException e) {
                throw new RuntimeException(e.getMessage());
            }
        });
    }
}
