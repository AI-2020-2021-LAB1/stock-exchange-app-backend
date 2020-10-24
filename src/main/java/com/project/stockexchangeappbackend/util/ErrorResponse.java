package com.project.stockexchangeappbackend.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private int status;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object errors;

}