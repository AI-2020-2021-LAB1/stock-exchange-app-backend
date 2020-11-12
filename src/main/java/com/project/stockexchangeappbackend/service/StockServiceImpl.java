package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.StockDTO;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.repository.StockRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Stock getStockById(Long id) {
        return stockRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Stock Not Found"));
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Page<Stock> getStocks(Pageable pageable, Specification<Stock> specification) {
        return stockRepository.findAll(specification, pageable);
    }

    @Override
    @LogicBusinessMeasureTime
    public Stock getStockByAbbreviation(String abbreviation) {
        return stockRepository.findByAbbreviationIgnoreCase(abbreviation).orElseThrow(() ->
                new EntityNotFoundException("Stock Not Found"));
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional
    public void updateStock(StockDTO stockDTO, String id) {
        if (stockRepository.findByNameOrAbbreviationIgnoreCase(stockDTO.getName(), stockDTO.getAbbreviation())
                .isPresent()) {
            throw new EntityExistsException("Stock with given name already exists");
        }
        Stock stock = getStockByIdOrAbbreviation(id);
        stock.setAbbreviation(stockDTO.getAbbreviation().trim());
        stock.setName(stockDTO.getName().trim());
        stockRepository.save(stock);
    }

    public Stock getStockByIdOrAbbreviation(String id) {
        try {
            return getStockById(new Long(id));
        } catch (NumberFormatException e) {
            return getStockByAbbreviation(id);
        }
    }

}

