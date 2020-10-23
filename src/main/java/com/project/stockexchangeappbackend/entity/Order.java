package com.project.stockexchangeappbackend.entity;

import lombok.*;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(generator = "ORDER_SEQUENCE")
    private Long id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(targetEntity = Stock.class)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, name = "amount")
    private Integer amount;

    @Column(nullable = false, name = "remaining_amount")
    private Integer remainingAmount;

    @Column(nullable = false, name = "type")
    private String type;

    @Column(nullable = false, name = "price_type")
    private String priceType;

    @Column(nullable = false, name = "date_creation")
    private OffsetDateTime dateCreation;

    @Column(nullable = false, name = "date_expiration")
    private OffsetDateTime dateExpiration;

    @Column(nullable = false, name = "date_closing")
    private OffsetDateTime dateClosing;

    @OneToMany(targetEntity = Transaction.class, mappedBy = "order")
    private List<Transaction> buyingOrders;

    @OneToMany(targetEntity = Transaction.class, mappedBy = "order")
    private List<Transaction> sellingOrders;
}
