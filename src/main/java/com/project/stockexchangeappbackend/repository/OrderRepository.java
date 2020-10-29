package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @DBQueryMeasureTime
    <S extends Order> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<Order> findById(Long id);

}
