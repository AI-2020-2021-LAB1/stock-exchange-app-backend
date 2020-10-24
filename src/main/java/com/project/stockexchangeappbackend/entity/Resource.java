package com.project.stockexchangeappbackend.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(generator = "RESOURCE_SEQUENCE")
    private Long id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(targetEntity = Stock.class)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, name = "amount")
    private Integer amount;
}
