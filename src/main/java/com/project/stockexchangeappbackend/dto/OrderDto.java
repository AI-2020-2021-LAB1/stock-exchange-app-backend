package com.project.stockexchangeappbackend.dto;


import com.project.stockexchangeappbackend.entity.OrderType;
import com.project.stockexchangeappbackend.entity.PriceType;
import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class OrderDto {

    private Long id;
    private int amount;
    private int remainingAmount;
    private OrderType orderType;
    private PriceType priceType;
    private OffsetDateTime dateCreation;
    private OffsetDateTime dateExpiration;
    private OffsetDateTime dateClosing;
}
