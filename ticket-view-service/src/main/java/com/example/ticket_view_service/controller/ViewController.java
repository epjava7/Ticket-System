package com.example.ticket_view_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {

    @GetMapping("/")
    public String homePage() {
        return "index";
    }
    
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(Model model) {
        return "dashboard";
    }

    @GetMapping("/ticket")
    public String ticketPage(@RequestParam Long id, Model model) {
        model.addAttribute("ticketId", id);
        return "ticket";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        return "signup";
    }

    @GetMapping("/users")
    public String usersPage(Model model) {
        return "users";
    }
}
