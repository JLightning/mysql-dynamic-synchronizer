package com.jhl.mds.consts;

import lombok.Getter;

public enum MigrationType {

    FULL_MIGRATION(0b0001),
    INCREMENTAL_MIGRATION(0b0010),
    FULL_INCREMENTAL_MIGRATION(0b0011);

    @Getter
    private final int code;

    MigrationType(int code) {
        this.code = code;
    }

    public static MigrationType getByCode(int taskTypeCode) {
        for (MigrationType migrationType : MigrationType.values()) {
            if (migrationType.code == taskTypeCode) {
                return migrationType;
            }
        }
        throw new RuntimeException("Not found");
    }

    public boolean isApplicable(MigrationType migrationType) {
        return (this.code & migrationType.code) == this.code;
    }
}
