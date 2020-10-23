package com.project.stockexchangeappbackend.Entities;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer id;

    @Column(nullable = false, name = "name")
    private String name;

    @Column(nullable = false, name = "abbreviation")
    private String abbreviation;

    @Column(nullable = false, name = "current_price")
    private Double currentPrice;

    @Column(nullable = false, name = "amount")
    private Integer amount;

    @OneToMany(targetEntity = Order.class, mappedBy = "stock", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(targetEntity = UserStock.class, mappedBy = "stock", cascade = CascadeType.ALL)
    private List<UserStock> userStocks;

}
