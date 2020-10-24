package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;


@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository repository;

    @Override
    public Stock getStockById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Stock Not Found"));
    }
}

