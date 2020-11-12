package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.StockIndexValueDTO;
import com.project.stockexchangeappbackend.entity.StockIndexValue;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface StockIndexValueService {

    void appendValue(StockIndexValue stockIndexValue);

    List<StockIndexValueDTO> getStockIndexValues (Long stockId, Specification<StockIndexValue> specification, Integer interval);

}
