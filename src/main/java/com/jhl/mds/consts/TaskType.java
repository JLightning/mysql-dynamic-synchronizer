package com.jhl.mds.consts;

import lombok.Getter;

public enum TaskType {

    MYSQL_TO_MYSQL(1),
    MYSQL_TO_REDIS(2);

    @Getter
    private int code;

    TaskType(int code) {
        this.code = code;
    }

    public static TaskType getByCode(int taskTypeCode) {
        for (TaskType tasktype : TaskType.values()) {
            if (tasktype.code == taskTypeCode) {
                return tasktype;
            }
        }
        throw new RuntimeException("Not found");
    }
}
