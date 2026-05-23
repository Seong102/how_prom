package com.howprom.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class Error403Controller {

    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }
}