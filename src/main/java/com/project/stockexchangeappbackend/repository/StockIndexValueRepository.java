package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.StockIndexValue;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockIndexValueRepository extends JpaRepository<StockIndexValue, Long>,
                                                    JpaSpecificationExecutor<StockIndexValue> {

    @Override
    @DBQueryMeasureTime
    <S extends StockIndexValue> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<StockIndexValue> findById(Long id);

    @Override
    @DBQueryMeasureTime
    List<StockIndexValue> findAll(@Nullable Specification<StockIndexValue> specification, Sort sort);

    @DBQueryMeasureTime
    Optional<StockIndexValue> findFirstByStockOrderByTimestampAsc(Stock stock);

    @DBQueryMeasureTime
    void deleteByStock(Stock stock);

}
