package com.jhl.mds.controllers.api;

import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.repositories.TaskRepository;
import com.jhl.mds.dto.ApiResponse;
import com.jhl.mds.dto.TaskDTO;
import com.jhl.mds.jsclientgenerator.JsClientController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/task-list")
@JsClientController(className = "TaskListApiClient", fileName = "task-list-api-client")
public class TaskListApiController {

    private TaskRepository taskRepository;
    private TaskDTO.Converter taskDTOConverter;

    @Autowired
    public TaskListApiController(
        TaskRepository taskRepository,
        TaskDTO.Converter taskDTOConverter
    ) {
        this.taskRepository = taskRepository;
        this.taskDTOConverter = taskDTOConverter;
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse exceptionHandler(Exception e) {
        return ApiResponse.error(e);
    }

    @GetMapping("/all")
    public ApiResponse<List<TaskDTO>> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        List<TaskDTO> taskDTOs = tasks.stream().map(task -> taskDTOConverter.from(task)).collect(Collectors.toList());
        return ApiResponse.success(taskDTOs);
    }
}
