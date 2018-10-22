package com.jhl.dds.querybuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InsertQuery {

    private final QueryBuilder queryBuilder;
    private List<Map<String, Object>> values;

    InsertQuery(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public InsertQuery values(List<Map<String, Object>> values) {
        this.values = values;
        return this;
    }

    private String buildColumns() {
        if (values == null || values.size() == 0) throw new RuntimeException("Empty values");
        Optional<String> columns = values.get(0).keySet().stream().map(c -> "`" + c + "`").reduce((s, s2) -> s + ", " + s2);

        return columns.get();
    }

    private String buildValues() {
        if (values == null || values.size() == 0) throw new RuntimeException("Empty values");
        List<String> firstColumns = new ArrayList<>(values.get(0).keySet());
        StringBuilder result = new StringBuilder();
        for (Map<String, Object> rowValue : values) {
            StringBuilder rowBuilder = new StringBuilder();
            for (String column : firstColumns) {
                Object value = rowValue.get(column);
                if (rowBuilder.length() > 0) rowBuilder.append(", ");
                if (value != null) rowBuilder.append("'").append(value).append("'");
                else rowBuilder.append("NULL");
            }

            if (result.length() > 0) result.append(", ");
            result.append("(").append(rowBuilder).append(")");
        }
        return result.toString();
    }

    public String build() {
        return "INSERT INTO " + queryBuilder.buildTableStr() + "(" + buildColumns() + ")" + " VALUES " + buildValues();
    }
}
