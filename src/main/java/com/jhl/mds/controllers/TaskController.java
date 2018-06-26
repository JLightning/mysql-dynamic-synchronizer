package com.jhl.mds.controllers;

import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.entities.Task;
import com.jhl.mds.dao.entities.TaskFieldMapping;
import com.jhl.mds.dao.repositories.MysqlServerRepository;
import com.jhl.mds.dao.repositories.TaskFieldMappingRepository;
import com.jhl.mds.dao.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/task")
public class TaskController {

    private TaskRepository taskRepository;
    private TaskFieldMappingRepository taskFieldMappingRepository;
    private MysqlServerRepository mysqlServerRepository;

    @Autowired
    public TaskController(TaskRepository taskRepository, TaskFieldMappingRepository taskFieldMappingRepository, MysqlServerRepository mysqlServerRepository) {
        this.taskRepository = taskRepository;
        this.taskFieldMappingRepository = taskFieldMappingRepository;
        this.mysqlServerRepository = mysqlServerRepository;
    }

    @GetMapping("/create")
    public String createAction() {
        return "task/create";
    }

    @GetMapping("/list")
    public String listAction(Model model) {
        List<Task> tasks = taskRepository.findAll();
        model.addAttribute("tasks", tasks);

        Map<Integer, String> mappingStringMap = new HashMap<>();
        tasks.forEach(task -> {
            List<TaskFieldMapping> mappings = taskFieldMappingRepository.findByFkTaskId(task.getTaskId());

            String mappingString = "";
            for (TaskFieldMapping mapping : mappings) {
                if (mappingString != "") mappingString += ", ";
                mappingString += mapping.getSourceField() + " -> " + mapping.getTargetField();
            }

            mappingStringMap.put(task.getTaskId(), mappingString);
        });

        model.addAttribute("mappingStringMap", mappingStringMap);

        List<Integer> serverIds = tasks.stream().flatMap(task -> Stream.of(task.getFkSourceServer(), task.getFkTargetServer())).distinct().collect(Collectors.toList());
        Map<Integer, MySQLServer> serverMap = mysqlServerRepository.findByServerId(serverIds).stream()
                .collect(Collectors.toMap(o -> (Integer) o.getServerId(), o -> o, (o1, o2) -> o1));

        model.addAttribute("serverMap", serverMap);
        return "task/list";
    }

    @GetMapping("/delete")
    public RedirectView deleteAction(@RequestParam int taskId) {
        taskRepository.deleteById(taskId);
        return new RedirectView("/task/list");
    }
}
