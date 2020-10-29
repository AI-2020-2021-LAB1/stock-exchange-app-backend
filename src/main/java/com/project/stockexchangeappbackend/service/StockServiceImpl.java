package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.repository.StockRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository repository;

    @Override
    @LogicBusinessMeasureTime
    public Stock getStockById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Stock Not Found"));
    }

}

