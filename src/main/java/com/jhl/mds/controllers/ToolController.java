package com.jhl.mds.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tool")
public class ToolController {

    @GetMapping("/structure-sync")
    public String tableStructureSyncAction() {
        return "tool/structure-sync";
    }
}
