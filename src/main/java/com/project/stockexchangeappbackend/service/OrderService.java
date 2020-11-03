package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


public interface OrderService {


    Order findOrderById(Long id);

    void createOrder(OrderDTO orderDTO);

    Page<Order> findAllOrders(Pageable pageable, Specification<Order> specification);
}
