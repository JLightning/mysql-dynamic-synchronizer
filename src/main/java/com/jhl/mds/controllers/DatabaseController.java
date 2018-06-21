package com.jhl.mds.controllers;

import com.jhl.mds.dao.entities.Db;
import com.jhl.mds.dao.repositories.DbRepository;
import com.jhl.mds.dto.DatabaseDTO;
import com.jhl.mds.services.common.FEMessageService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;

@Controller
@RequestMapping("/database")
public class DatabaseController {

    private DbRepository dbRepository;
    private FEMessageService feMessageService;

    public DatabaseController(
            DbRepository dbRepository,
            FEMessageService feMessageService
    ) {
        this.dbRepository = dbRepository;
        this.feMessageService = feMessageService;
    }

    @GetMapping("/list")
    public String listAction(Model model) {
        model.addAttribute("databases", dbRepository.findAll(Sort.by(Sort.Direction.DESC, "databaseId")));
        return "database/list";
    }

    @GetMapping("/add")
    public String addAction() {
        return "database/add";
    }

    @PostMapping(value = "/add-post")
    public RedirectView addPostAction(@Valid @ModelAttribute("dto") DatabaseDTO dto) {
        try {
            // test connection
            DriverManager.getConnection("jdbc:mysql://" + dto.getHost() + ":" + dto.getPort() + "/" + dto.getDatabase(), dto.getUsername(), dto.getPassword());
        } catch (SQLException e) {
            feMessageService.addError("Unable to connect to database");
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

        try {
            dbRepository.save(db);
        } catch (Exception e) {
            feMessageService.addError("Error when adding new database: " + e.getMessage());
        }

        return new RedirectView("/database/add");
    }
}
