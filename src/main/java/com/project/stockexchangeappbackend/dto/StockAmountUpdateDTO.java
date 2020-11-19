package com.project.stockexchangeappbackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
@ApiModel(description = "Object used to get information about updating stock amount")
public class StockAmountUpdateDTO {

    @NotNull(message = "This field is required")
    @ApiModelProperty(notes = "User's id")
    Long userId;

    @NotNull(message = "This field is required")
    @ApiModelProperty(notes = "The stock amount to update")
    Integer amount;
}
