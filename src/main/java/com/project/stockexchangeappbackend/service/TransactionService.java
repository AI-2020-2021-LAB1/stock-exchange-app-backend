package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Transaction;

public interface TransactionService {

    Transaction findTransactionById(Long id);
}
