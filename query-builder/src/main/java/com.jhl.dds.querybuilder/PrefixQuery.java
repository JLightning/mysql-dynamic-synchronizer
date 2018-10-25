package com.jhl.dds.querybuilder;

public class PrefixQuery {

    private final QueryBuilder queryBuilder;
    private final String prefix;

    PrefixQuery(QueryBuilder queryBuilder, String prefix) {
        this.queryBuilder = queryBuilder;
        this.prefix = prefix;
    }

    public String build() {
        return prefix + " " + queryBuilder.buildTableStr();
    }
}
