package com.project.stockexchangeappbackend.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "STOCKS")
public class Stock {

    @Id
    @GeneratedValue(generator = "STOCK_SEQUENCE")
    private Long id;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    @Column(name = "ABBREVIATION", unique = true, nullable = false)
    private String abbreviation;

    @Column(name = "CURRENT_PRICE", precision = 14, scale = 2, nullable = false)
    private BigDecimal currentPrice;

    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;

    @Column(name = "PRICE_CHANGE_RATIO", nullable = false)
    private Double priceChangeRatio;

}
