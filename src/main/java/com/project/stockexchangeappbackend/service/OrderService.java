package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.entity.AllOrders;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;


public interface OrderService {

    AllOrders findOrderById(Long id);

    void createOrder(OrderDTO orderDTO);

    List<Order> getActiveBuyingOrders();

    List<Order> getActiveSellingOrdersByStockAndPriceLessThanEqual(Stock stock, BigDecimal maximalPrice);

    void moveInactiveOrders();

    Page<AllOrders> getOwnedOrders(Pageable pageable, Specification<AllOrders> specification);

    Page<AllOrders> findAllOrders(Pageable pageable, Specification<AllOrders> specification);

    void deactivateOrder(Long id);

    List<Order> getOrdersByUser(Long id);

}
