package com.project.stockexchangeappbackend.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
@ApiModel(description = "Owned stocks' object stored in database.")
public class ResourceDTO {

    @ApiModelProperty(notes = "Owned stocks' id.")
    private Long id;

    @ApiModelProperty(notes = "Owned stocks' name.")
    private String name;

    @ApiModelProperty(notes = "Owned stocks' abbreviation.")
    private String abbreviation;

    @ApiModelProperty(notes = "Owned units' amount of stocks.")
    private Integer amount;

}
