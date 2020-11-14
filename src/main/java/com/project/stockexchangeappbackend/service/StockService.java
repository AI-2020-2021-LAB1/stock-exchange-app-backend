package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.CreateStockDTO;
import com.project.stockexchangeappbackend.dto.StockDTO;
import com.project.stockexchangeappbackend.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface StockService {

    Stock getStockById(Long id);
    Page<Stock> getStocks(Pageable pageable, Specification<Stock> specification);
    Stock getStockByAbbreviation(String abbreviation);
    List<Stock> getAllStocks();
    Stock updateStock(Stock stock);
    void updateStock(StockDTO stockDTO, String id);
    Stock getStockByIdOrAbbreviation(String id);
    void createStock(CreateStockDTO stockDTO);
    void deleteStock(Long id);

}
