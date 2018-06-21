package com.jhl.mds.controllers;

import com.jhl.mds.dao.entities.MysqlServer;
import com.jhl.mds.dao.repositories.MysqlServerRepository;
import com.jhl.mds.dto.MysqlServerDTO;
import com.jhl.mds.services.common.FEMessageService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

@Controller
@RequestMapping("/mysql-server")
public class MysqlServerController {

    private MysqlServerRepository mysqlServerRepository;
    private FEMessageService feMessageService;

    public MysqlServerController(
            MysqlServerRepository mysqlServerRepository,
            FEMessageService feMessageService
    ) {
        this.mysqlServerRepository = mysqlServerRepository;
        this.feMessageService = feMessageService;
    }

    @GetMapping("/list")
    public String listAction(Model model) {
        model.addAttribute("mysqlServers", mysqlServerRepository.findAll(Sort.by(Sort.Direction.DESC, "serverId")));
        return "mysql-server/list";
    }

    @GetMapping("/add")
    public String addAction() {
        return "mysql-server/add";
    }

    @PostMapping(value = "/add-post")
    public RedirectView addPostAction(@Valid @ModelAttribute("dto") MysqlServerDTO dto) {
        try {
            // test connection
            DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort(), dto.getUsername(), dto.getPassword());
        } catch (SQLException e) {
            feMessageService.addError("Unable to connect to server");
            return new RedirectView("/mysql-server/add");
        }
        Date now = new Date();
        MysqlServer mysqlServer = MysqlServer.builder()
                .name(dto.getName())
                .host(dto.getHost())
                .port(dto.getPort())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            mysqlServerRepository.save(mysqlServer);
        } catch (Exception e) {
            feMessageService.addError("Error when adding new server: " + e.getMessage());
        }

        return new RedirectView("/mysql-server/list");
    }

    @GetMapping("/delete")
    public RedirectView deleteAction(@RequestParam int serverId) {
        try {
            mysqlServerRepository.deleteById(serverId);
        } catch (Exception e) {
            feMessageService.addError("Error when deleting server: " + e.getMessage());
        }
        return new RedirectView("/mysql-server/list");
    }
}
