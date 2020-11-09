package com.project.stockexchangeappbackend.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "RESOURCES")
public class Resource {

    @Id
    @GeneratedValue(generator = "RESOURCE_SEQUENCE")
    private Long id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "USER_ID", nullable = false, updatable = false, referencedColumnName = "ID")
    private User user;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "STOCK_ID", nullable = false, updatable = false, referencedColumnName = "ID")
    private Stock stock;

    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;

}
