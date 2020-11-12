package com.project.stockexchangeappbackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
@ApiModel(description = "Stock object stored in database.")
public class StockDTO {

    @ApiModelProperty(notes = "The stock's id.")
    private Long id;

    @ApiModelProperty(notes = "The stock's name.")
    @NotNull(message = "This field is required.")
    private String name;

    @Size(min = 3, max = 3, message = "Size of the abbreviation must be equal {max}.")
    @ApiModelProperty(notes = "The stock's abbreviation.")
    @NotNull(message = "This field is required.")
    private String abbreviation;

    @ApiModelProperty(notes = "The stock's average current price per unit.")
    @NotNull(message = "This field is required.")
    private BigDecimal currentPrice;

    @ApiModelProperty(notes = "The stock's amount of units.")
    @NotNull(message = "This field is required.")
    private Integer amount;

}
