package com.project.stockexchangeappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private BigDecimal money;
}
