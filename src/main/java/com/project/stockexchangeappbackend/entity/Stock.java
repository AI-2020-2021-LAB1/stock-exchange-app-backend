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
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(generator = "STOCK_SEQUENCE")
    private Long id;

    @Column(nullable = false, name = "name", unique = true)
    private String name;

    @Column(nullable = false, name = "abbreviation")
    private String abbreviation;

    @Column(nullable = false, name = "current_price", precision = 14, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, name = "amount")
    private Integer amount;

}
