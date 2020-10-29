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

    @ManyToOne(targetEntity = User.class, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false, updatable = false, referencedColumnName = "ID")
    private User user;

    @ManyToOne(targetEntity = Stock.class, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "stock_id", nullable = false, updatable = false, referencedColumnName = "ID")
    private Stock stock;

    @Column(nullable = false, name = "amount", updatable = false)
    private Integer amount;

    @Column(nullable = false, name = "remaining_amount")
    private Integer remainingAmount;

    @Column(nullable = false, name = "type")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(nullable = false, name = "price_type")
    @Enumerated(EnumType.STRING)
    private PriceType priceType;

    @Column(nullable = false, name = "date_creation")
    private OffsetDateTime dateCreation;

    @Column(nullable = false, name = "date_expiration")
    private OffsetDateTime dateExpiration;

    @Column(nullable = false, name = "date_closing")
    private OffsetDateTime dateClosing;

    @OneToMany(targetEntity = Transaction.class, mappedBy = "buyingOrder")
    private List<Transaction> buyingOrders;

    @OneToMany(targetEntity = Transaction.class, mappedBy = "sellingOrder")
    private List<Transaction> sellingOrders;

}
