package com.project.stockexchangeappbackend.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

    @PostMapping("/api/register")
    public String register() {
        return "register";
    }

    @PostMapping("/api/confirm_email")
    public String confirmEmail() {
        return "confirm_email";
    }
}
