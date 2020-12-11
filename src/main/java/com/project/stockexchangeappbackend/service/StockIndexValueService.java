package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.StockIndexValueDTO;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.StockIndexValue;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockIndexValueService {

    void appendValues(Collection<StockIndexValue> stockIndexValues);
    List<StockIndexValueDTO> getStockIndexValues(Long stockId, Specification<StockIndexValue> specification, Integer interval);
    Optional<StockIndexValue> getFirstStockIndexValueBeforeMinutesAgo(Stock stock, Integer minutes);

}
