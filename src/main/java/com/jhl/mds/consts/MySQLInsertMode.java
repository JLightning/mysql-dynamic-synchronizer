package com.jhl.mds.consts;

import com.jhl.mds.jsclientgenerator.JsClientDTO;
import lombok.Getter;

@JsClientDTO(fileName = "mysql-insert-mode", className = "MySQLInsertMode")
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
