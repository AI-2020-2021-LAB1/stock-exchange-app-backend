package com.project.stockexchangeappbackend.rest;

import com.project.stockexchangeappbackend.dto.StockDto;
import com.project.stockexchangeappbackend.service.StockService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final ModelMapper mapper;

    @GetMapping
    public String getStocks() {
        return "stocks";
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockDto> getStockDetails(@PathVariable long id) {
        StockDto stockdto = mapper.map(stockService.getStockById(id), StockDto.class);
        return new ResponseEntity<>(stockdto, HttpStatus.OK);
    }
}
