package com.project.stockexchangeappbackend.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(generator = "USER_SEQUENCE")
    private Long id;

    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Column(nullable = true, name = "email")
    private String email;

    @Column(nullable = false, name = "password")
    private String password;

    @Column(nullable = false, name = "role")
    @Enumerated(EnumType.STRING)
    private String role;

    @Column(nullable = false, name = "money", precision = 14, scale = 2)
    private BigDecimal money;

    @OneToMany(targetEntity = Order.class, mappedBy = "user")
    private List<Order> orders;

    @OneToMany(targetEntity = Resource.class, mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Resource> userStocks;
}
