package com.jhl.mds.dto;

import lombok.Getter;

public enum MySQLMigrationMode {

    INSERT("INSERT"),
    INSERT_IGNORE("INSERT IGNORE"),
    REPLACE("REPLACE");

    @Getter
    private String syntax;

    MySQLMigrationMode(String syntax) {
        this.syntax = syntax;
    }
}
