package com.example.DiplomaGeneration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping
    public String login(@RequestParam(name = "error", required = false) String error,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверные учетные данные");
        }
        return "login";
    }
}
