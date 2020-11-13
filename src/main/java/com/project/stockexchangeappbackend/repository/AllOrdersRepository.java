package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.AllOrders;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllOrdersRepository extends JpaRepository<AllOrders, Long>, JpaSpecificationExecutor<AllOrders> {

    @Override
    @DBQueryMeasureTime
    Page<AllOrders> findAll(@Nullable Specification<AllOrders> specification, Pageable pageable);

    @Override
    @DBQueryMeasureTime
    Optional<AllOrders> findById(Long id);


}
