package com.jhl.mds.dto;

import com.jhl.mds.consts.MySQLInsertMode;
import com.jhl.mds.consts.TaskType;
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
    private List<SimpleFieldMappingDTO> mapping;
    private Table source;
    private Table target;
    private TaskType taskType;
    private MySQLInsertMode insertMode;

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
            List<SimpleFieldMappingDTO> mapping = taskFieldMappings.stream()
                    .map(o -> new SimpleFieldMappingDTO(o.getSourceField(), o.getTargetField())).collect(Collectors.toList());
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
                    .taskType(TaskType.getByCode(task.getTaskType()))
                    .insertMode(MySQLInsertMode.valueOf(task.getInsertType()))
                    .mapping(mapping)
                    .build();
        }
    }
}
