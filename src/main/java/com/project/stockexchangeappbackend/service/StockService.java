package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface StockService {


    Stock getStockById(Long id);
    Page<Stock> getStocks(Pageable pageable, Specification<Stock> specification);
    Stock getStockByAbbreviation(String abbreviation);

}
