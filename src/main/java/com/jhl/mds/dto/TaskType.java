package com.jhl.mds.dto;

import lombok.Getter;

public enum TaskType {

    FULL_MIGRATION(0b0001),
    INCREMENTAL_MIGRATION(0b0010),
    FULL_INCREMENTAL_MIGRATION(0b0011);

    @Getter
    private final int code;

    TaskType(int code) {
        this.code = code;
    }
}
