package com.jhl.mds.dto.migration;

import com.jhl.mds.consts.MigrationAction;
import com.jhl.mds.consts.MySQLInsertMode;
import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import com.jhl.mds.dao.repositories.MySQLServerRepository;
import com.jhl.mds.dao.repositories.TaskFieldMappingRepository;
import com.jhl.mds.dao.repositories.TaskFilterRepository;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TableInfoDTO;
import com.jhl.mds.dto.TaskDTO;
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
public class MySQL2MySQLMigrationDTO implements MySQLSourceMigrationDTO, FilterableMigrationDTO {

    private int taskId;
    private TableInfoDTO source;
    private TableInfoDTO target;
    private List<SimpleFieldMappingDTO> mapping;
    private MySQLInsertMode insertMode;
    private int migrationActionCode;
    private List<String> filters;
    private List<String> targetColumns;

    @Service
    public static class Converter {

        private TaskRepository taskRepository;
        private TaskFieldMappingRepository taskFieldMappingRepository;
        private TaskFilterRepository taskFilterRepository;
        private MySQLServerRepository mySQLServerRepository;
        private MySQLServerDTO.Converter serverDTOConverter;

        @Autowired
        public Converter(
                TaskRepository taskRepository,
                TaskFieldMappingRepository taskFieldMappingRepository,
                TaskFilterRepository taskFilterRepository,
                MySQLServerRepository mySQLServerRepository,
                MySQLServerDTO.Converter serverDTOConverter
        ) {

            this.taskRepository = taskRepository;
            this.taskFieldMappingRepository = taskFieldMappingRepository;
            this.taskFilterRepository = taskFilterRepository;
            this.mySQLServerRepository = mySQLServerRepository;
            this.serverDTOConverter = serverDTOConverter;
        }

        public MySQL2MySQLMigrationDTO from(int taskId) {
            return from(taskRepository.getOne(taskId));
        }

        public MySQL2MySQLMigrationDTO from(Task task) {
            List<TaskFieldMapping> mapping = taskFieldMappingRepository.findByFkTaskId(task.getTaskId());

            List<SimpleFieldMappingDTO> mappingDTOs = mapping.stream().map(m -> new SimpleFieldMappingDTO(m.getSourceField(), m.getTargetField())).collect(Collectors.toList());

            MySQLServer sourceServer = mySQLServerRepository.findByServerId(task.getFkSourceServer());
            MySQLServer targetServer = mySQLServerRepository.findByServerId(task.getFkTargetServer());

            TableInfoDTO sourceTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(sourceServer), task.getSourceDatabase(), task.getSourceTable());
            TableInfoDTO targetTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(targetServer), task.getTargetDatabase(), task.getTargetTable());

            return MySQL2MySQLMigrationDTO.builder()
                    .taskId(task.getTaskId())
                    .mapping(mappingDTOs)
                    .source(sourceTableInfoDTO)
                    .target(targetTableInfoDTO)
                    .insertMode(MySQLInsertMode.valueOf(task.getInsertMode()))
                    .filters(taskFilterRepository.findFilterByTaskId(task.getTaskId()))
                    .build();
        }

        public MySQL2MySQLMigrationDTO from(TaskDTO taskDTO) {
            MySQLServer sourceServer = mySQLServerRepository.findByServerId(taskDTO.getSource().getServerId());
            MySQLServer targetServer = mySQLServerRepository.findByServerId(taskDTO.getTarget().getServerId());

            TableInfoDTO sourceTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(sourceServer), taskDTO.getSource().getDatabase(), taskDTO.getSource().getTable());
            TableInfoDTO targetTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(targetServer), taskDTO.getTarget().getDatabase(), taskDTO.getTarget().getTable());

            return MySQL2MySQLMigrationDTO.builder()
                    .mapping(taskDTO.getMapping())
                    .source(sourceTableInfoDTO)
                    .target(targetTableInfoDTO)
                    .insertMode(taskDTO.getInsertMode())
                    .filters(taskDTO.getFilters())
                    .build();
        }
    }
}
