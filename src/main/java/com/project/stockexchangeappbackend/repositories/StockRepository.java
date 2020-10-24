package com.project.stockexchangeappbackend.repositories;

import com.project.stockexchangeappbackend.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
