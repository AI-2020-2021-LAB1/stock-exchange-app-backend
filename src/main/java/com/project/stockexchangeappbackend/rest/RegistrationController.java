package com.project.stockexchangeappbackend.rest;

import com.project.stockexchangeappbackend.dto.ErrorResponse;
import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.service.UserService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@AllArgsConstructor
@Api(value = "Users", description = "REST API for users' management", tags = "Users")
@ApiResponses({
        @ApiResponse(code = 400, message = "The request could not be understood or was missing required parameters.",
                response = ErrorResponse.class)
})
public class RegistrationController {

    private final UserService userService;

    @PostMapping("/register")
    @ApiOperation(value = "Register new user", response = RegistrationUserDTO.class)
    @ApiResponses({@ApiResponse(code = 200, message = "User was successfully registered."),
            @ApiResponse(code = 409, message = "User with given email already exists.", response = ErrorResponse.class)})
    public void register(@ApiParam(value = "User object to registration in database", required = true)
                             @RequestBody @Valid RegistrationUserDTO registrationUserDTO) {
        userService.registerUser(registrationUserDTO);
    }

    @PostMapping("/confirm_email")
    @ApiIgnore
    public String confirmEmail() {
        return "confirm_email";
    }

}
