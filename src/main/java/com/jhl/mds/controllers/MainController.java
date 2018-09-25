package com.jhl.mds.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @RequestMapping(value = {"/", "/task/**", "/util/**", "/server/**"})
    public String indexAction() {
        return "index";
    }
}
