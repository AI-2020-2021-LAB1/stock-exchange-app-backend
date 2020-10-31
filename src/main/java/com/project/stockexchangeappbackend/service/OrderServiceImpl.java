package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.repository.ArchivedOrderRepository;
import com.project.stockexchangeappbackend.repository.OrderRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ArchivedOrderRepository archivedOrderRepository;
    private final ModelMapper modelMapper;

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseGet(() -> modelMapper.map(archivedOrderRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Order Not Found")), Order.class));
    }

}
