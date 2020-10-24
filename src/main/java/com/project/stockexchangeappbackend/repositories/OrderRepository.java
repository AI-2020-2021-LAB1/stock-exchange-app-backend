package com.project.stockexchangeappbackend.repositories;

import com.project.stockexchangeappbackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
