package com.project.stockexchangeappbackend.Entities;

import lombok.*;

//import javax.validation.constraints.Email;
import javax.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, name = "ID", nullable = false)
    private Integer id;

    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Column(nullable = true, name = "email")
    //@Email
    private String email;

    @Column(nullable = false, name = "password")
    private String password;

    @Column(nullable = false, name = "role")
    private String role;

    @Column(nullable = false, name = "money")
    private Double money;

    @OneToMany(targetEntity = Order.class, mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Order> orders;

    @OneToMany(targetEntity = UserStock.class, mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<UserStock> userStocks;
}
