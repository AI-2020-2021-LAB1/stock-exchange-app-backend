package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

}
