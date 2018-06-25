package com.jhl.mds.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskDTO {
    private int taskId;
    private String taskName;
    private List<Mapping> mapping;
    private Table source;
    private Table target;

    @Data
    public static class Mapping {
        private String sourceField;
        private String targetField;
    }

    @Data
    public static class Table {
        private int serverId;
        private String database;
        private String table;
    }
}
