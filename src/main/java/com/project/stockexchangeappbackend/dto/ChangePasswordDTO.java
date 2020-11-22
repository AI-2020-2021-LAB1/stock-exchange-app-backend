package com.project.stockexchangeappbackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
@ApiModel(description = "User object stored in database.")
public class ChangePasswordDTO {

    @ApiModelProperty(notes = "The new password.")
    @NotBlank(message = "This field is required.")
    private String newPassword;

    @ApiModelProperty(notes = "The old password.")
    @NotBlank(message = "This field is required.")
    private String oldPassword;
}
