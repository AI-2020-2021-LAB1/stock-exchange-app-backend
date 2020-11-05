package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.Stock;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    Order findOrderById(Long id);
    void createOrder(OrderDTO orderDTO);
    List<Order> getActiveBuyingOrders();
    List<Order> getActiveSellingOrdersByStockAndPriceLessThanEqual(Stock stock, BigDecimal maximalPrice);
    void moveInactiveOrders();

}
