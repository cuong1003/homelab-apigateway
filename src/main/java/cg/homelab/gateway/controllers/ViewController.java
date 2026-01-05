package cg.homelab.gateway.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewController {
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    @GetMapping("/")
    public String landingPage() {
        return "redirect:/login";
    }
    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }
}
