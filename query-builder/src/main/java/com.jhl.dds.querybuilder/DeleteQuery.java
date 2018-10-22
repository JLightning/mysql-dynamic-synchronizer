package com.jhl.dds.querybuilder;

import java.util.Map;

public class DeleteQuery {

    private QueryBuilder queryBuilder;

    DeleteQuery(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public DeleteQuery where(Map<String, Object> where) {
        queryBuilder.where(where);
        return this;
    }

    public String build() {

        return "DELETE FROM " + queryBuilder.buildTableStr() + queryBuilder.buildWhere();
    }
}
