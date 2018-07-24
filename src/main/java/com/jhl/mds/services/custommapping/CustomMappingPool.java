package com.jhl.mds.services.custommapping;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class CustomMappingPool {

    private static final int POOL_SIZE = 4;
    private ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
    private List<CustomMapping> customMappingList = new ArrayList<>();
    private Integer roundRobin = 0;

    @PostConstruct
    private void init() {
        for (int i = 0; i < POOL_SIZE; i++) {
            customMappingList.add(new CustomMapping());
        }
    }

    public Future<String> resolve(String input, Map<String, Object> data) {
        final int tmpRoundRobin;

        roundRobin++;
        if (roundRobin >= POOL_SIZE) roundRobin = 0;
        tmpRoundRobin = roundRobin % POOL_SIZE;

        return executor.submit(() -> customMappingList.get(tmpRoundRobin).resolve(input, data));
    }
}
