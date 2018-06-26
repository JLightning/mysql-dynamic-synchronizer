package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import com.jhl.mds.dao.repositories.TaskFieldMappingRepository;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.ApiResponse;
import com.jhl.mds.dto.TaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/task")
public class TaskApiController {

    private TaskRepository taskRepository;
    private TaskFieldMappingRepository taskFieldMappingRepository;

    @Autowired
    public TaskApiController(TaskRepository taskRepository, TaskFieldMappingRepository taskFieldMappingRepository) {
        this.taskRepository = taskRepository;
        this.taskFieldMappingRepository = taskFieldMappingRepository;
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

            for (TaskDTO.Mapping mapping : dto.getMapping()) {
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
}
