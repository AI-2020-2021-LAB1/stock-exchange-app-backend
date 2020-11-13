package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long>, JpaSpecificationExecutor<Stock> {

    @Override
    @DBQueryMeasureTime
    <S extends Stock> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<Stock> findById(Long id);

    @Override
    @DBQueryMeasureTime
    Page<Stock> findAll(@Nullable Specification<Stock> var1, Pageable var2);

    @Override
    @DBQueryMeasureTime
    List<Stock> findAll();

    @DBQueryMeasureTime
    Optional<Stock> findByAbbreviationIgnoreCase(String abbreviation);

    @DBQueryMeasureTime
    Optional<Stock> findByNameIgnoreCaseOrAbbreviationIgnoreCase(String name, String abbreviation);
  
    @DBQueryMeasureTime
    Optional<Stock> findByNameIgnoreCase(String name);

}
