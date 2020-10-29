package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Transaction;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Override
    @DBQueryMeasureTime
    <S extends Transaction> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<Transaction> findById(Long id);

}
