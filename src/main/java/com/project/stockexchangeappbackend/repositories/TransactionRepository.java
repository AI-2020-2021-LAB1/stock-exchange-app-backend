package com.project.stockexchangeappbackend.repositories;

import com.project.stockexchangeappbackend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAll();
}
