package com.example.DiplomaGeneration.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth-choice")
public class AuthChoiceController {

    @GetMapping
    public String authChoice(Model model,
                             @RequestParam(name = "error", required = false) String error,
                             @RequestParam(name = "logout", required = false) String logout) {

        if (error != null) {
            model.addAttribute("error", "Неверные учетные данные");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }

        return "auth-choice";
    }
}