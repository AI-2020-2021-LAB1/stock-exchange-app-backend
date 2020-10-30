package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.ArchivedOrder;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArchivedOrderRepository extends JpaRepository<ArchivedOrder, Long> {

    @DBQueryMeasureTime
    <S extends ArchivedOrder> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<ArchivedOrder> findById(Long id);

}
