package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @DBQueryMeasureTime
    <S extends Order> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<Order> findById(Long id);

    @Override
    @DBQueryMeasureTime
    @Query(value = "SELECT * FROM ALL_ORDERS",
            countQuery = "SELECT COUNT(*) FROM ALL_ORDERS",
            nativeQuery = true)
    Page<Order> findAll(@Nullable Specification specification, Pageable pageable);


}
