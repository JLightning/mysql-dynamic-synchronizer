package com.jhl.mds.dto;

import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private int taskId;
    private String taskName;
    private List<Mapping> mapping;
    private Table source;
    private Table target;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Mapping {
        private String sourceField;
        private String targetField;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Table {
        private int serverId;
        private String database;
        private String table;
    }

    @Component
    public static class Converter {

        public TaskDTO from(Task task, List<TaskFieldMapping> taskFieldMappings) {
            List<Mapping> mapping = taskFieldMappings.stream()
                    .map(o -> Mapping.builder()
                            .sourceField(o.getSourceField())
                            .targetField(o.getTargetField())
                            .build()
                    ).collect(Collectors.toList());
            return TaskDTO.builder()
                    .taskId(task.getTaskId())
                    .taskName(task.getName())
                    .source(Table.builder()
                            .serverId(task.getFkSourceServer())
                            .database(task.getSourceDatabase())
                            .table(task.getSourceTable())
                            .build())
                    .target(Table.builder()
                            .serverId(task.getFkTargetServer())
                            .database(task.getTargetDatabase())
                            .table(task.getTargetTable())
                            .build())
                    .mapping(mapping)
                    .build();
        }
    }
}
