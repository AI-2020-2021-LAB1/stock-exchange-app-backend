package com.project.stockexchangeappbackend.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController {
    @GetMapping
    public String getStocks() {
        return "stocks";
    }

    @GetMapping("/{id}")
    public String getStockDetails(@PathVariable final String id) {
        return "stock_details " + id;
    }
}
