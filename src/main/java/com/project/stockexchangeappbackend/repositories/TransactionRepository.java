package com.project.stockexchangeappbackend.repositories;

import com.project.stockexchangeappbackend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
