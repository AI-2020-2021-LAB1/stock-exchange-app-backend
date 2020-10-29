package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.repository.OrderRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    @Override
    @LogicBusinessMeasureTime
    public Order findOrderById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order Not Found"));
    }

}
