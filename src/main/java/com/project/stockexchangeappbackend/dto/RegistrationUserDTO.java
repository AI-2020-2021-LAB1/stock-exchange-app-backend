package com.project.stockexchangeappbackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@ApiModel(description = "User object to registration in database.")
public class RegistrationUserDTO {

    @NotBlank(message = "This field is required.")
    @Length(min = 1, max = 255, message = "This field can be {max} characters long")
    @ApiModelProperty(notes = "The user's first name.", required = true, allowableValues="range[1, 255]")
    private String firstName;

    @NotBlank(message = "This field is required.")
    @Length(min = 1, max = 255, message = "This field can be {max} characters long")
    @ApiModelProperty(notes = "The user's last name.", required = true, allowableValues="range[1, 255]")
    private String lastName;

    @NotBlank(message = "This field is required.")
    @Email(regexp = "^\\S+@\\S+\\.\\S+$", message = "Email must be valid.")
    @Length(min = 1, max = 255, message = "This field can be {max} characters long")
    @ApiModelProperty(notes = "The user's email.", required = true, allowableValues="range[1, 255]")
    private String email;

    @NotBlank(message = "This field is required.")
    @Length(min = 6, max = 255, message = "This field must be between {min} and {max} characters long.")
    @ApiModelProperty(notes = "The user's password.", required = true, allowableValues="range[6, 255]")
    private String password;

}
