package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.entity.Order;

public interface OrderService {

    Order findOrderById(Long id);
    void createOrder(OrderDTO orderDTO);

}
