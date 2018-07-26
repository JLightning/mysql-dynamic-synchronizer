package com.jhl.mds.dto;

import com.jhl.mds.consts.MySQLInsertMode;
import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import com.jhl.mds.dao.repositories.MySQLServerRepository;
import com.jhl.mds.dao.repositories.TaskFieldMappingRepository;
import com.jhl.mds.dao.repositories.TaskRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrationDTO {

    private int taskId;
    private TableInfoDTO source;
    private TableInfoDTO target;
    private List<SimpleFieldMappingDTO> mapping;
    private MySQLInsertMode insertMode;
    private List<String> filters;
    private List<String> targetColumns;

    @Service
    public static class Converter {

        private TaskRepository taskRepository;
        private TaskFieldMappingRepository taskFieldMappingRepository;
        private MySQLServerRepository mySQLServerRepository;
        private MySQLServerDTO.Converter serverDTOConverter;

        @Autowired
        public Converter(
                TaskRepository taskRepository,
                TaskFieldMappingRepository taskFieldMappingRepository,
                MySQLServerRepository mySQLServerRepository,
                MySQLServerDTO.Converter serverDTOConverter
        ) {

            this.taskRepository = taskRepository;
            this.taskFieldMappingRepository = taskFieldMappingRepository;
            this.mySQLServerRepository = mySQLServerRepository;
            this.serverDTOConverter = serverDTOConverter;
        }

        public MigrationDTO from(int taskId) {
            return from(taskRepository.getOne(taskId));
        }

        public MigrationDTO from(Task task) {
            List<TaskFieldMapping> mapping = taskFieldMappingRepository.findByFkTaskId(task.getTaskId());

            List<SimpleFieldMappingDTO> mappingDTOs = mapping.stream().map(m -> new SimpleFieldMappingDTO(m.getSourceField(), m.getTargetField())).collect(Collectors.toList());

            MySQLServer sourceServer = mySQLServerRepository.findByServerId(task.getFkSourceServer());
            MySQLServer targetServer = mySQLServerRepository.findByServerId(task.getFkTargetServer());

            TableInfoDTO sourceTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(sourceServer), task.getSourceDatabase(), task.getSourceTable());
            TableInfoDTO targetTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(targetServer), task.getTargetDatabase(), task.getTargetTable());

            return MigrationDTO.builder()
                    .taskId(task.getTaskId())
                    .mapping(mappingDTOs)
                    .source(sourceTableInfoDTO)
                    .target(targetTableInfoDTO)
                    .insertMode(MySQLInsertMode.valueOf(task.getInsertMode()))
                    .build();
        }

        public MigrationDTO from(TaskDTO taskDTO) {
            MySQLServer sourceServer = mySQLServerRepository.findByServerId(taskDTO.getSource().getServerId());
            MySQLServer targetServer = mySQLServerRepository.findByServerId(taskDTO.getTarget().getServerId());

            TableInfoDTO sourceTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(sourceServer), taskDTO.getSource().getDatabase(), taskDTO.getSource().getTable());
            TableInfoDTO targetTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(targetServer), taskDTO.getTarget().getDatabase(), taskDTO.getTarget().getTable());

            return MigrationDTO.builder()
                    .mapping(taskDTO.getMapping())
                    .source(sourceTableInfoDTO)
                    .target(targetTableInfoDTO)
                    .insertMode(taskDTO.getInsertMode())
                    .build();
        }
    }
}
