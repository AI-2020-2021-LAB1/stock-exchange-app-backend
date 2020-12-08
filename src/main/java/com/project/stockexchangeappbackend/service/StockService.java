package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.CreateStockDTO;
import com.project.stockexchangeappbackend.dto.EditStockNameDTO;
import com.project.stockexchangeappbackend.dto.UpdateStockAmountDTO;
import com.project.stockexchangeappbackend.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;

public interface StockService {

    Stock getStockById(Long id);
    Page<Stock> getStocks(Pageable pageable, Specification<Stock> specification);
    Stock getStockByAbbreviation(String abbreviation);
    List<Stock> getAllStocks();
    Stock updateStock(Stock stock);
    void updateStocks(Collection<Stock> stocks);
    void updateStock(EditStockNameDTO stock, String id);
    Stock getStockByIdOrAbbreviation(String id);
    void createStock(CreateStockDTO stockDTO, String tag);
    void deleteStock(Long id);
    void updateStockAmount(Long stockId, UpdateStockAmountDTO updateStockAmount);

}
