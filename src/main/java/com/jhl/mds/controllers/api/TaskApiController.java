package com.jhl.mds.controllers.api;

import com.jhl.mds.consts.MySQLInsertMode;
import com.jhl.mds.consts.TaskType;
import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import com.jhl.mds.dao.entities.TaskFilter;
import com.jhl.mds.dao.repositories.TaskFieldMappingRepository;
import com.jhl.mds.dao.repositories.TaskFilterRepository;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.*;
import com.jhl.mds.events.IncrementalStatusUpdateEvent;
import com.jhl.mds.events.ProgressUpdateEvent;
import com.jhl.mds.jsclientgenerator.JsClientController;
import com.jhl.mds.services.migration.mysql2mysql.FullMigrationService;
import com.jhl.mds.services.migration.mysql2mysql.IncrementalMigrationService;
import com.jhl.mds.services.mysql.MySQLCommonService;
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
    private TaskFilterRepository taskFilterRepository;
    private MySQLCommonService mySQLCommonService;
    private FullMigrationService fullMigrationService;
    private IncrementalMigrationService incrementalMigrationService;
    private TaskDTO.Converter taskDTOConverter;
    private MigrationDTO.Converter fullMigrationDTOConverter;
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public TaskApiController(
            TaskRepository taskRepository,
            TaskFieldMappingRepository taskFieldMappingRepository,
            TaskFilterRepository taskFilterRepository,
            MySQLCommonService mySQLCommonService,
            FullMigrationService fullMigrationService,
            IncrementalMigrationService incrementalMigrationService,
            TaskDTO.Converter taskDTOConverter,
            MigrationDTO.Converter fullMigrationDTOConverter,
            SimpMessagingTemplate simpMessagingTemplate
    ) {
        this.taskRepository = taskRepository;
        this.taskFieldMappingRepository = taskFieldMappingRepository;
        this.taskFilterRepository = taskFilterRepository;
        this.mySQLCommonService = mySQLCommonService;
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
            Task.TaskBuilder taskBuilder = Task.builder();
            if (dto.getTaskId() != 0) {
                taskBuilder = taskRepository.getOne(dto.getTaskId()).toBuilder();
            }
            Task task = taskBuilder
                    .name(dto.getTaskName())
                    .fkSourceServer(sourceTaskDTOTable.getServerId())
                    .sourceDatabase(sourceTaskDTOTable.getDatabase())
                    .sourceTable(sourceTaskDTOTable.getTable())
                    .fkTargetServer(targetTaskDTOTable.getServerId())
                    .targetDatabase(targetTaskDTOTable.getDatabase())
                    .targetTable(targetTaskDTOTable.getTable())
                    .taskType(dto.getTaskType().getCode())
                    .insertMode(dto.getInsertMode().toString())
                    .fullMigrationProgress(0)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            taskRepository.save(task);

            taskFieldMappingRepository.deleteByFkTaskId(task.getTaskId());

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

            taskFilterRepository.deleteByFkTaskId(task.getTaskId());

            for (String filter : dto.getFilters()) {
                TaskFilter taskFilter = TaskFilter.builder()
                        .fkTaskId(task.getTaskId())
                        .filter(filter)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                taskFilterRepository.save(taskFilter);
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

        return ApiResponse.success(taskDTOConverter.from(task, taskMapping, taskFilterRepository.findByFkTaskId(taskId)));
    }

    @SubscribeMapping("/channel/task/full-migration-progress/{taskId}")
    public FullMigrationProgressDTO getFullMigrationTaskProgressWs(@DestinationVariable int taskId) {
        return new FullMigrationProgressDTO(fullMigrationService.isTaskRunning(taskId), fullMigrationService.getProgress(taskId));
    }

    @GetMapping("/detail/{taskId}/start-full-migration")
    public ApiResponse<Boolean> startFullMigrationTask(@PathVariable int taskId) {
        try {
            MigrationDTO migrationDTO = fullMigrationDTOConverter.from(taskId);
            fullMigrationService.queue(migrationDTO);
        } catch (Exception e) {
            return ApiResponse.error(e);
        }

        return ApiResponse.success(true);
    }

    @GetMapping("/detail/{taskId}/truncate-and-start-full-migration")
    public ApiResponse<Boolean> truncateAndStartFullMigrationTask(@PathVariable int taskId) {
        try {
            MigrationDTO migrationDTO = fullMigrationDTOConverter.from(taskId);
            mySQLCommonService.truncateTable(migrationDTO.getTarget());

            fullMigrationService.queue(migrationDTO);
        } catch (Exception e) {
            return ApiResponse.error(e);
        }

        return ApiResponse.success(true);
    }

    @GetMapping("/detail/{taskId}/start-incremental-migration")
    public ApiResponse<Boolean> startIncrementalMigrationTask(@PathVariable int taskId) {
        MigrationDTO migrationDTO = fullMigrationDTOConverter.from(taskId);
        incrementalMigrationService.run(migrationDTO);

        taskRepository.updateIncrementalMigrationActive(taskId, true);

        return ApiResponse.success(true);
    }

    @GetMapping("/detail/{taskId}/stop-incremental-migration")
    public ApiResponse<Boolean> stopIncrementalMigrationTask(@PathVariable int taskId) {
        MigrationDTO migrationDTO = fullMigrationDTOConverter.from(taskId);
        incrementalMigrationService.stop(migrationDTO);

        taskRepository.updateIncrementalMigrationActive(taskId, false);

        return ApiResponse.success(true);
    }

    @SubscribeMapping("/channel/task/incremental-migration-progress/{taskId}")
    public IncrementalMigrationProgressDTO getIncrementalMigrationProgressWs(@DestinationVariable int taskId) {
        return incrementalMigrationService.getIncrementalMigrationProgress(taskId);
    }

    @GetMapping("/get-task-types")
    public ApiResponse<TaskType[]> getTaskTypes() {
        return ApiResponse.success(TaskType.values());
    }

    @GetMapping("/get-insert-modes")
    public ApiResponse<MySQLInsertMode[]> getInsertModes() {
        return ApiResponse.success(MySQLInsertMode.values());
    }

    @EventListener
    @Async
    public void onFullMigrationTaskProgressUpdate(ProgressUpdateEvent<MigrationDTO> event) {
        MigrationDTO dto = event.getDto();
        FullMigrationProgressDTO fullMigrationProgressDTO = new FullMigrationProgressDTO(event.isRunning(), Math.round(event.getProgress()));
        simpMessagingTemplate.convertAndSend("/app/channel/task/full-migration-progress/" + dto.getTaskId(), fullMigrationProgressDTO);
    }

    @EventListener
    @Async
    public void onIncrementalMigrationTaskStatusUpdate(IncrementalStatusUpdateEvent event) {
        simpMessagingTemplate.convertAndSend("/app/channel/task/incremental-migration-progress/" + event.getTaskId(), event);
    }
}
