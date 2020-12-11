package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.StockIndexValue;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
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
    <S extends StockIndexValue> List<S> saveAll(Iterable<S> var1);

    @Override
    @DBQueryMeasureTime
    Optional<StockIndexValue> findById(Long id);

    @Override
    @DBQueryMeasureTime
    List<StockIndexValue> findAll(@Nullable Specification<StockIndexValue> specification, Sort sort);

    @DBQueryMeasureTime
    List<StockIndexValue> findByStockIdInAndTimestampIsBeforeOrderByTimestampAsc(List<Long> stock, OffsetDateTime time);

    @DBQueryMeasureTime
    void deleteByStock(Stock stock);

    @DBQueryMeasureTime
    Optional<StockIndexValue> findFirstByStockAndTimestampBeforeOrderByTimestampDesc(Stock stock, OffsetDateTime dateTime);

    @DBQueryMeasureTime
    @Query(nativeQuery = true,
            value = "SELECT siv.stock_id FROM stock_index_values siv GROUP BY siv.stock_id HAVING COUNT(siv) > :max_records")
    List<Long> findStockWithExceedHistory(@Param("max_records") Integer maxRecords);

}
