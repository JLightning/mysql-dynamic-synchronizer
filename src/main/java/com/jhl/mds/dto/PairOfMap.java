package com.jhl.mds.dto;

import lombok.experimental.Delegate;
import org.springframework.data.util.Pair;

import java.util.Map;

public class PairOfMap {

    @Delegate
    private Pair<Map<String, Object>, Map<String, Object>> pair;

    private PairOfMap(Pair<Map<String, Object>, Map<String, Object>> pair) {
        this.pair = pair;
    }

    public static PairOfMap of(Map<String, Object> first, Map<String, Object> second) {
        return new PairOfMap(Pair.of(first, second));
    }
}
