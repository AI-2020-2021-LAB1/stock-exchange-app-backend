package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.repository.StockRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository repository;

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Stock getStockById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Stock Not Found"));
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Page<Stock> getStocks(Pageable pageable, Specification<Stock> specification) {
        return repository.findAll(specification, pageable);
    }

    @Override
    @LogicBusinessMeasureTime
    public Stock getStockByAbbreviation(String abbreviation) {
        return repository.findByAbbreviationIgnoreCase(abbreviation).orElseThrow(() ->
                new EntityNotFoundException("Stock Not Found"));
    }

    @Override
    @LogicBusinessMeasureTime
    public List<Stock> getAllStocks() {
        return repository.findAll();
    }

    @Override
    @LogicBusinessMeasureTime
    public Stock updateStock(Stock stock) {
        return repository.save(stock);
    }

}

