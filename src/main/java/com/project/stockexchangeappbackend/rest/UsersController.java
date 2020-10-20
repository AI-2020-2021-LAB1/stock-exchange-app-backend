package com.project.stockexchangeappbackend.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UsersController {

    @GetMapping
    public String getDetails() {
        return "details";
    }

    @PutMapping
    public String changeDetails()
    {
        return "change_details";
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getUsers()
    {
        return "users_list";
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getUser(@PathVariable final String id) {
        return "user_details " + id;
    }
}
