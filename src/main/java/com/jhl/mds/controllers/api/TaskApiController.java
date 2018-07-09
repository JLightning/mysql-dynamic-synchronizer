package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import com.jhl.mds.dao.repositories.MySQLServerRepository;
import com.jhl.mds.dao.repositories.TaskFieldMappingRepository;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.*;
import com.jhl.mds.services.migration.mysql2mysql.FullMigrationService;
import com.jhl.mds.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/task")
public class TaskApiController {

    private TaskRepository taskRepository;
    private TaskFieldMappingRepository taskFieldMappingRepository;
    private MySQLServerRepository mySQLServerRepository;
    private FullMigrationService fullMigrationService;
    private TaskDTO.Converter taskDTOConverter;
    private MySQLServerDTO.Converter serverDTOConverter;

    @Autowired
    public TaskApiController(
            TaskRepository taskRepository,
            TaskFieldMappingRepository taskFieldMappingRepository,
            MySQLServerRepository mySQLServerRepository,
            FullMigrationService fullMigrationService,
            TaskDTO.Converter taskDTOConverter,
            MySQLServerDTO.Converter serverDTOConverter
    ) {
        this.taskRepository = taskRepository;
        this.taskFieldMappingRepository = taskFieldMappingRepository;
        this.mySQLServerRepository = mySQLServerRepository;
        this.fullMigrationService = fullMigrationService;
        this.taskDTOConverter = taskDTOConverter;
        this.serverDTOConverter = serverDTOConverter;
    }

    @PostMapping("/create")
    public ApiResponse<TaskDTO> createTaskAction(@RequestBody TaskDTO dto) {
        try {
            Date now = new Date();
            TaskDTO.Table sourceTaskDTOTable = dto.getSource();
            TaskDTO.Table targetTaskDTOTable = dto.getTarget();
            Task task = Task.builder().name(dto.getTaskName())
                    .fkSourceServer(sourceTaskDTOTable.getServerId())
                    .sourceDatabase(sourceTaskDTOTable.getDatabase())
                    .sourceTable(sourceTaskDTOTable.getTable())
                    .fkTargetServer(targetTaskDTOTable.getServerId())
                    .targetDatabase(targetTaskDTOTable.getDatabase())
                    .targetTable(targetTaskDTOTable.getTable())
                    .taskType(TaskType.FULL_INCREMENTAL_MIGRATION.getCode())
                    .fullMigrationProgress(0)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            taskRepository.save(task);

            for (SimpleFieldMappingDTO mapping : dto.getMapping()) {
                TaskFieldMapping fieldMapping = TaskFieldMapping.builder()
                        .fkTaskId(task.getTaskId())
                        .sourceField(mapping.getSourceField())
                        .targetField(mapping.getTargetField())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                taskFieldMappingRepository.save(fieldMapping);
            }

            MySQLServer sourceServer = mySQLServerRepository.findByServerId(sourceTaskDTOTable.getServerId());
            MySQLServer targetServer = mySQLServerRepository.findByServerId(sourceTaskDTOTable.getServerId());

            TableInfoDTO sourceTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(sourceServer), sourceTaskDTOTable.getDatabase(), sourceTaskDTOTable.getTable());
            TableInfoDTO targetTableInfoDTO = new TableInfoDTO(serverDTOConverter.from(targetServer), targetTaskDTOTable.getDatabase(), targetTaskDTOTable.getTable());

            dto.setTaskId(task.getTaskId());

            fullMigrationService.queue(FullMigrationDTO.builder()
                    .taskId(task.getTaskId())
                    .mapping(dto.getMapping())
                    .source(sourceTableInfoDTO)
                    .target(targetTableInfoDTO)
                    .build());

            return ApiResponse.success(dto);
        } catch (JpaSystemException e) {
            return ApiResponse.error(e.getRootCause().getMessage());
        } catch (Exception e) {
            return ApiResponse.error(e);
        }
    }

    @GetMapping("/detail/{taskId}")
    public ApiResponse<TaskDTO> getTaskAction(@PathVariable int taskId) {
        Optional<Task> optTask = taskRepository.findById(taskId);
        if (!optTask.isPresent()) {
            return ApiResponse.error("Task not found");
        }

        List<TaskFieldMapping> taskMapping = Util.defaultIfNull(taskFieldMappingRepository.findByFkTaskId(taskId), new ArrayList<>());

        return ApiResponse.success(taskDTOConverter.from(optTask.get(), taskMapping));
    }

    @GetMapping("/detail/{taskId}/full-migration-progress")
    public ApiResponse<Double> getFullMigrationTaskProgress(@PathVariable int taskId) {
        return ApiResponse.success(fullMigrationService.getProgress(taskId));
    }
}
