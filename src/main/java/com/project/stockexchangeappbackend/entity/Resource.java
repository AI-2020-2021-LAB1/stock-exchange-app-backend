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

    @ManyToOne(targetEntity = User.class, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false, updatable = false, referencedColumnName = "ID")
    private User user;

    @ManyToOne(targetEntity = Stock.class, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "stock_id", nullable = false, updatable = false, referencedColumnName = "ID")
    private Stock stock;

    @Column(nullable = false, name = "amount")
    private Integer amount;

}
