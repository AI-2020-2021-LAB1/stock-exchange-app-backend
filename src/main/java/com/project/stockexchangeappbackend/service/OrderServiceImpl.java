package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.OrderDto;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;

    @Override
    public Order findOrderById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order Not Found"));
    }
}
