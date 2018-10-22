package com.jhl.mds.services.mysql;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MySQLWhereService {

    public String build(Map<String, Object> input) {
        StringBuilder wherePart = new StringBuilder();
        for (Map.Entry<String, Object> e : input.entrySet()) {
            if (wherePart.length() > 0) wherePart.append(" AND ");
            if (e.getValue() != null) {
                wherePart.append(e.getKey()).append(" = ").append("'").append(e.getValue()).append("'");
            } else {
                wherePart.append(e.getKey()).append(" IS NULL");
            }
        }

        return wherePart.toString();
    }
}
