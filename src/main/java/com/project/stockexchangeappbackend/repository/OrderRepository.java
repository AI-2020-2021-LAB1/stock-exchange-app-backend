package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.OrderType;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @DBQueryMeasureTime
    <S extends Order> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<Order> findById(Long id);

    @DBQueryMeasureTime
    List<Order> findByOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(OrderType orderType, OffsetDateTime expirationDate);

    @DBQueryMeasureTime
    List<Order> findByStockAndOrderTypeAndPriceIsLessThanEqualAndDateExpirationIsAfterAndDateClosingIsNullOrderByPrice(
            Stock stock, OrderType orderType, BigDecimal price, OffsetDateTime expirationDate);

    @DBQueryMeasureTime
    List<Order> findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(Stock stock, User user,
                                                               OrderType orderType, OffsetDateTime dateExpiration);

    @DBQueryMeasureTime
    List<Order> findByDateExpirationIsBeforeOrRemainingAmountOrDateClosingIsNotNull(
            OffsetDateTime offsetDateTime, int remainingAmount);

}
