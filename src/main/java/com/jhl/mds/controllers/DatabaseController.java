package com.jhl.mds.controllers;

import com.jhl.mds.dao.entities.Db;
import com.jhl.mds.dao.repositories.DbRepository;
import com.jhl.mds.dto.DatabaseDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.sql.*;
import java.util.Date;

@Controller
@RequestMapping("/database")
public class DatabaseController {

    private DbRepository dbRepository;

    public DatabaseController(
            DbRepository dbRepository
    ) {
        this.dbRepository = dbRepository;
    }

    @GetMapping("/list")
    public String listAction(Model model) {
        model.addAttribute("databases", dbRepository.findAll());
        return "database/list";
    }

    @GetMapping("/add")
    public String addAction() {
        return "database/add";
    }

    @PostMapping(value = "/add-post")
    public RedirectView addPostAction(@Valid @ModelAttribute("dto") DatabaseDTO dto) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort() + "/" + dto.getDatabase(), dto.getUsername(), dto.getPassword());
        } catch (SQLException e) {
            return new RedirectView("/database/add");
        }
        Date now = new Date();
        Db db = Db.builder()
                .name(dto.getName())
                .dbName(dto.getDatabase())
                .host(dto.getHost())
                .port(dto.getPort())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .createdAt(now)
                .updatedAt(now)
                .build();

        dbRepository.save(db);

        return new RedirectView("/database/add");
    }
}
