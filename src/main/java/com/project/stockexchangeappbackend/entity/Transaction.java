package com.project.stockexchangeappbackend.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(generator = "TRANSACTION_SEQUENCE")
    private Long id;

    @Column(nullable = false, name = "date")
    private OffsetDateTime date;

    @Column(nullable = false, name = "amount", updatable = false)
    private Integer amount;

    @Column(nullable = false, name = "unit_price", precision = 14, scale = 2, updatable = false)
    private BigDecimal unitPrice;

    @ManyToOne(targetEntity = Order.class, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_id", nullable = false, updatable = false, referencedColumnName = "ID")
    private Order buyingOrder;

    @ManyToOne(targetEntity = Order.class, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "order_id", nullable = false, updatable = false, referencedColumnName = "ID")
    private Order sellingOrder;
}
