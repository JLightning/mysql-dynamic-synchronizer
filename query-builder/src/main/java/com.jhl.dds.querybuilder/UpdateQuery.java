package com.jhl.dds.querybuilder;

import java.util.Map;

public class UpdateQuery {

    private final QueryBuilder queryBuilder;
    private Map<String, Object> set;

    UpdateQuery(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public String build() {
        return "UPDATE " + queryBuilder.buildTableStr() + " SET " + buildSet() + queryBuilder.buildWhere();
    }

    public UpdateQuery set(Map<String, Object> set) {
        this.set = set;
        return this;
    }

    private String buildSet() {
        StringBuilder setPart = new StringBuilder();
        for (Map.Entry<String, Object> e : set.entrySet()) {
            if (setPart.length() > 0) setPart.append(", ");
            setPart.append(e.getKey()).append(" = ").append("'").append(e.getValue()).append("'");
        }
        return setPart.toString();
    }

    public UpdateQuery where(Map<String, Object> where) {
        queryBuilder.where(where);
        return this;
    }
}
