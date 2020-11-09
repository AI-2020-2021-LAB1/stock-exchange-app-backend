package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public interface TransactionService {

    Transaction findTransactionById(Long id);

    void makeTransaction(Order buyingOrder, Order sellingOrder, int amount, BigDecimal pricePerUnit);

    Page<Transaction> findAllTransactions(Pageable pageable, Specification<Transaction> specification);

    Page<Transaction> getOwnedTransactions(Pageable pageable, Specification<Transaction> specification,
                                           boolean isSeller, boolean isBuyer);

    Page<Transaction> getTransactionsByOrder(Pageable pageable, Specification<Transaction> specification,
                                             long orderId);
}
