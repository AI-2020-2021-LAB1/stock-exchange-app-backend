package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.entity.AllOrders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


public interface OrderService {

    AllOrders findOrderById(Long id);

    void createOrder(OrderDTO orderDTO);

    Page<AllOrders> findAllOrders(Pageable pageable, Specification<AllOrders> specification);
}
