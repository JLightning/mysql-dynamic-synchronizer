package com.jhl.mds.consts;

public enum MigrationAction {
    INSERT(0b001),
    UPDATE(0b010),
    DELETE(0b100);

    private int code;

    MigrationAction(int code) {
        this.code = code;
    }

    public boolean isApplicable(int migrationActionCode) {
        return (code & migrationActionCode) == code;
    }
}
