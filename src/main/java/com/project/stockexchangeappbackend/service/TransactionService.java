package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.Transaction;

import java.math.BigDecimal;

public interface TransactionService {

    Transaction findTransactionById(Long id);
    void makeTransaction(Order buyingOrder, Order sellingOrder, int amount, BigDecimal pricePerUnit);

}
