package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @DBQueryMeasureTime
    <S extends Stock> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<Stock> findById(Long id);

}
