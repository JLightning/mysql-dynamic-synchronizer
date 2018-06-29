package com.jhl.mds.controllers;

import com.jhl.mds.dao.entities.MySQLServer;
import com.jhl.mds.dao.repositories.MySQLServerRepository;
import com.jhl.mds.dto.MySQLServerDTO;
import com.jhl.mds.services.common.FEMessageService;
import com.jhl.mds.services.mysql.MySQLConnectionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/mysql-server")
public class MysqlServerController {

    private MySQLServerRepository mySQLServerRepository;
    private FEMessageService feMessageService;
    private MySQLConnectionPool mySQLConnectionPool;

    @Autowired
    public MysqlServerController(
            MySQLServerRepository mySQLServerRepository,
            FEMessageService feMessageService,
            MySQLConnectionPool mySQLConnectionPool
    ) {
        this.mySQLServerRepository = mySQLServerRepository;
        this.feMessageService = feMessageService;
        this.mySQLConnectionPool = mySQLConnectionPool;
    }

    @GetMapping("/list")
    public String listAction(Model model) {
        model.addAttribute("mysqlServers", mySQLServerRepository.findAll(Sort.by(Sort.Direction.DESC, "serverId")));
        return "mysql-server/list";
    }

    @GetMapping("/add")
    public String addAction() {
        return "mysql-server/add";
    }

    @GetMapping("/edit")
    public String editAction(@RequestParam int serverId, Model model) {
        Optional<MySQLServer> opt = mySQLServerRepository.findById(serverId);
        if (!opt.isPresent()) {
            feMessageService.addError("No server with id " + serverId + " found");
            return "redirect:/mysql-server/list";
        }
        model.addAttribute("mysqlServer", opt.get());
        return "mysql-server/add";
    }

    @PostMapping(value = "/add-post")
    public RedirectView addPostAction(@Valid @ModelAttribute("dto") MySQLServerDTO dto) {
        String errorRedirectUrl = dto.getServerId() == 0 ? "/mysql-server/add" : "/mysql-server/edit?serverId=" + dto.getServerId();

        try {
            // test connection
            mySQLConnectionPool.getConnection(dto);
        } catch (SQLException e) {
            feMessageService.addError("Unable to connect to server");
            return new RedirectView(errorRedirectUrl);
        }
        Date now = new Date();
        MySQLServer mySQLServer = MySQLServer.builder()
                .serverId(dto.getServerId())
                .name(dto.getName())
                .host(dto.getHost())
                .port(dto.getPort())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            mySQLServerRepository.save(mySQLServer);
        } catch (Exception e) {
            feMessageService.addError("Error when adding new server: " + e.getMessage());
            return new RedirectView(errorRedirectUrl);
        }

        return new RedirectView("/mysql-server/list");
    }

    @GetMapping("/delete")
    public RedirectView deleteAction(@RequestParam int serverId) {
        try {
            mySQLServerRepository.deleteById(serverId);
        } catch (Exception e) {
            feMessageService.addError("Error when deleting server: " + e.getMessage());
        }
        return new RedirectView("/mysql-server/list");
    }
}
