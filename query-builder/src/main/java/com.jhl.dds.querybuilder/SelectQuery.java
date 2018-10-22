package com.jhl.dds.querybuilder;

import org.apache.commons.lang3.StringUtils;

public class SelectQuery {

    private QueryBuilder queryBuilder;
    private Iterable<String> columns;

    SelectQuery(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public String build() {
        String columnsStr = columns == null ? "*" : columnListToString(columns);

        return "SELECT " + columnsStr + " FROM " + queryBuilder.buildTableStr() + queryBuilder.buildWhere();
    }

    public SelectQuery columns(Iterable<String> columns) {
        this.columns = columns;
        return this;
    }

    private String columnListToString(Iterable<?> columns) {
        return "`" + StringUtils.join(columns, "`, `") + "`";
    }
}
