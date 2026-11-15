package com.hospital.appointment.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Spring MVC controller serving Thymeleaf frontend HTML templates.
 */
@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/doctors")
    public String doctors() {
        return "doctors";
    }

    @GetMapping("/appointments")
    public String appointments() {
        return "appointments";
    }

    @GetMapping("/doctor/slots")
    public String doctorSlots() {
        return "doctor-slots";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
}
