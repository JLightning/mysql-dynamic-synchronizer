package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import com.jhl.mds.dao.repositories.TaskFieldMappingRepository;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.ApiResponse;
import com.jhl.mds.dto.SimpleFieldMappingDTO;
import com.jhl.mds.dto.TaskDTO;
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
    private TaskDTO.Converter taskDTOConverter;

    @Autowired
    public TaskApiController(
            TaskRepository taskRepository,
            TaskFieldMappingRepository taskFieldMappingRepository,
            TaskDTO.Converter taskDTOConverter
    ) {
        this.taskRepository = taskRepository;
        this.taskFieldMappingRepository = taskFieldMappingRepository;
        this.taskDTOConverter = taskDTOConverter;
    }

    @PostMapping("/create")
    public ApiResponse<TaskDTO> createTaskAction(@RequestBody TaskDTO dto) {
        try {
            Date now = new Date();
            Task task = Task.builder().name(dto.getTaskName())
                    .fkSourceServer(dto.getSource().getServerId())
                    .sourceDatabase(dto.getSource().getDatabase())
                    .sourceTable(dto.getSource().getTable())
                    .fkTargetServer(dto.getTarget().getServerId())
                    .targetDatabase(dto.getTarget().getDatabase())
                    .targetTable(dto.getTarget().getTable())
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
        Optional<Task> optTask = taskRepository.findById(taskId);
        if (!optTask.isPresent()) {
            return ApiResponse.error("Task not found");
        }

        List<TaskFieldMapping> taskMapping = Util.defaultIfNull(taskFieldMappingRepository.findByFkTaskId(taskId), new ArrayList<>());

        return ApiResponse.success(taskDTOConverter.from(optTask.get(), taskMapping));
    }
}
