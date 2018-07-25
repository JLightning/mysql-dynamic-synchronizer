package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import com.jhl.mds.dao.repositories.TaskFieldMappingRepository;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.*;
import com.jhl.mds.events.ProgressUpdateEvent;
import com.jhl.mds.jsclientgenerator.JsClientController;
import com.jhl.mds.services.migration.mysql2mysql.FullMigrationService;
import com.jhl.mds.services.migration.mysql2mysql.IncrementalMigrationService;
import com.jhl.mds.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/task")
@JsClientController(className = "TaskApiClient", fileName = "task-api-client")
public class TaskApiController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private TaskRepository taskRepository;
    private TaskFieldMappingRepository taskFieldMappingRepository;
    private FullMigrationService fullMigrationService;
    private IncrementalMigrationService incrementalMigrationService;
    private TaskDTO.Converter taskDTOConverter;
    private FullMigrationDTO.Converter fullMigrationDTOConverter;
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public TaskApiController(
            TaskRepository taskRepository,
            TaskFieldMappingRepository taskFieldMappingRepository,
            FullMigrationService fullMigrationService,
            IncrementalMigrationService incrementalMigrationService,
            TaskDTO.Converter taskDTOConverter,
            FullMigrationDTO.Converter fullMigrationDTOConverter,
            SimpMessagingTemplate simpMessagingTemplate
    ) {
        this.taskRepository = taskRepository;
        this.taskFieldMappingRepository = taskFieldMappingRepository;
        this.fullMigrationService = fullMigrationService;
        this.incrementalMigrationService = incrementalMigrationService;
        this.taskDTOConverter = taskDTOConverter;
        this.fullMigrationDTOConverter = fullMigrationDTOConverter;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse exceptionHandler(Exception e) {
        return ApiResponse.error(e);
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

            dto.setTaskId(task.getTaskId());
            return ApiResponse.success(dto);
        } catch (JpaSystemException e) {
            return ApiResponse.error(e.getRootCause().getMessage());
        } catch (Exception e) {
            return ApiResponse.error(e);
        }
    }

    @GetMapping("/detail/{taskId}")
    public ApiResponse<TaskDTO> getTaskAction(@PathVariable int taskId) {
        Task task = taskRepository.getOne(taskId);
        List<TaskFieldMapping> taskMapping = Util.defaultIfNull(taskFieldMappingRepository.findByFkTaskId(taskId), new ArrayList<>());

        return ApiResponse.success(taskDTOConverter.from(task, taskMapping));
    }

    @SubscribeMapping("/channel/task/full-migration-progress/{taskId}")
    public FullMigrationProgressDTO getFullMigrationTaskProgressWs(@DestinationVariable int taskId) {
        return new FullMigrationProgressDTO(fullMigrationService.isTaskRunning(taskId), fullMigrationService.getProgress(taskId));
    }

    @EventListener
    @Async
    public void onFullMigrationTaskProgressUpdate(ProgressUpdateEvent<FullMigrationDTO> event) {
        FullMigrationDTO dto = event.getDto();
        FullMigrationProgressDTO fullMigrationProgressDTO = new FullMigrationProgressDTO(event.isRunning(), Math.round(event.getProgress()));
        simpMessagingTemplate.convertAndSend("/app/channel/task/full-migration-progress/" + dto.getTaskId(), fullMigrationProgressDTO);
    }

    @GetMapping("/detail/{taskId}/start-full-migration")
    public ApiResponse<Boolean> startFullMigrationTask(@PathVariable int taskId) {
        try {
            FullMigrationDTO fullMigrationDTO = fullMigrationDTOConverter.from(taskId);
            fullMigrationService.queue(fullMigrationDTO);
        } catch (Exception e) {
            return ApiResponse.error(e);
        }

        return ApiResponse.success(true);
    }

    @GetMapping("/detail/{taskId}/start-incremental-migration")
    public ApiResponse<Boolean> startIncrementalMigrationTask(@PathVariable int taskId) {
        FullMigrationDTO fullMigrationDTO = fullMigrationDTOConverter.from(taskId);
        incrementalMigrationService.run(fullMigrationDTO);

        taskRepository.updateIncrementalMigrationActive(taskId, true);

        return ApiResponse.success(true);
    }

    @GetMapping("/detail/{taskId}/stop-incremental-migration")
    public ApiResponse<Boolean> stopIncrementalMigrationTask(@PathVariable int taskId) {
        FullMigrationDTO fullMigrationDTO = fullMigrationDTOConverter.from(taskId);
        incrementalMigrationService.stop(fullMigrationDTO);

        taskRepository.updateIncrementalMigrationActive(taskId, false);

        return ApiResponse.success(true);
    }
}
