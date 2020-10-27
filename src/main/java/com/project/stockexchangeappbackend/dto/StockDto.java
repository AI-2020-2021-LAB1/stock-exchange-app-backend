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
public class StockDto {
    private Long id;
    private String name;
    private String abbreviation;
    private BigDecimal currentPrice;
    private Integer amount;
}
