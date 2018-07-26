package com.jhl.mds.consts;

import lombok.Getter;

public enum MySQLInsertMode {

    INSERT("INSERT"),
    INSERT_IGNORE("INSERT IGNORE"),
    REPLACE("REPLACE");

    @Getter
    private String syntax;

    MySQLInsertMode(String syntax) {
        this.syntax = syntax;
    }
}
