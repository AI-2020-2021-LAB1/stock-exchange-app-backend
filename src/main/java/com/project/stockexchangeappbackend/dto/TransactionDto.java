package com.project.stockexchangeappbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class TransactionDto {
    private Long id;
    private OffsetDateTime date;
    private Integer amount;
    private BigDecimal unitPrice;
    private Long buyingOrderId;
    private Long sellingOrderId;
}
