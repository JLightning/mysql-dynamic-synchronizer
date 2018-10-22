package com.jhl.dds.querybuilder;

import java.util.Map;

public class QueryBuilder {

    private String table;
    private String database;
    private Map<String, Object> where;

    public SelectQuery selectFrom(String database, String table) {
        this.table = table;
        this.database = database;
        return new SelectQuery(this);
    }

    public SelectQuery selectFrom(String table) {
        return selectFrom(null, table);
    }

    public InsertQuery insertInto(String database, String table) {
        this.table = table;
        this.database = database;
        return new InsertQuery(this);
    }

    public InsertQuery insertInto(String table) {
        return insertInto(null, table);
    }

    public UpdateQuery update(String database, String table) {
        this.database = database;
        this.table = table;
        return new UpdateQuery(this);
    }

    public UpdateQuery update(String table) {
        return update(null, table);
    }

    public DeleteQuery deleteFrom(String database, String table) {
        this.database = database;
        this.table = table;
        return new DeleteQuery(this);
    }

    public DeleteQuery deleteFrom(String table) {
        return deleteFrom(null, table);
    }

    void where(Map<String, Object> where) {
        this.where = where;
    }

    String buildWhere() {
        if (where == null) return "";
        StringBuilder wherePart = new StringBuilder();
        for (Map.Entry<String, Object> e : where.entrySet()) {
            if (wherePart.length() > 0) wherePart.append(" AND ");
            if (e.getValue() != null) {
                wherePart.append(e.getKey()).append(" = ").append("'").append(e.getValue()).append("'");
            } else {
                wherePart.append(e.getKey()).append(" IS NULL");
            }
        }

        return " WHERE " + wherePart.toString();
    }

    String buildTableStr() {
        String databaseStr = database == null ? "" : "`" + database + "`.";
        String tableStr = "`" + table + "`";
        return databaseStr + tableStr;
    }
}
