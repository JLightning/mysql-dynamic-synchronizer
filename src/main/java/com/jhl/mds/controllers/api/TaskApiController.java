package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.ApiResponse;
import com.jhl.mds.dto.TaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/task")
public class TaskApiController {

    private TaskRepository taskRepository;

    @Autowired
    public TaskApiController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping("/create")
    public ApiResponse<Boolean> createTaskAction(@RequestBody TaskDTO dto) {
        Task task = Task.builder().name(dto.getTaskName())
                .fkSourceServer(dto.getSource().getServerId())
                .sourceDatabse(dto.getSource().getDatabase())
                .sourceTable(dto.getSource().getTable())
                .fkTargetServer(dto.getTarget().getServerId())
                .targetDatabase(dto.getTarget().getDatabase())
                .targetTable(dto.getTarget().getTable())
                .build();

        taskRepository.save(task);
        return null;
    }
}
