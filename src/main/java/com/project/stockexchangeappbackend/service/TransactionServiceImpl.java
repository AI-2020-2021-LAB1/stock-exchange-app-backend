package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Transaction;
import com.project.stockexchangeappbackend.repository.TransactionRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;

    @Override
    @LogicBusinessMeasureTime
    public Transaction findTransactionById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Transaction Not Found"));
    }

}
