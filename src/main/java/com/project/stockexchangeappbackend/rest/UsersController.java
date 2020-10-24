package com.project.stockexchangeappbackend.rest;

import com.project.stockexchangeappbackend.dto.UserDto;
import com.project.stockexchangeappbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;
    private final ModelMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getDetails(@PathVariable Long id) {
        UserDto userDto = mapper.map(userService.findUserById(id),UserDto.class);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
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
