package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.ArchivedOrder;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.Transaction;
import com.project.stockexchangeappbackend.repository.ArchivedOrderRepository;
import com.project.stockexchangeappbackend.repository.OrderRepository;
import com.project.stockexchangeappbackend.repository.TransactionRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ArchivedOrderRepository archivedOrderRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    @Override
    @LogicBusinessMeasureTime
    public Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction Not Found"));
    }

    @Override
    @Transactional // TODO: uni test
    public void createTransaction(Order buyingOrder, Order sellingOrder, int amount, BigDecimal pricePerUnit) {
        ArchivedOrder archivedBuyingOrder = archivedOrderRepository.findById(buyingOrder.getId())
                .orElseGet(() -> modelMapper.map(buyingOrder, ArchivedOrder.class));
        ArchivedOrder archivedSellingOrder = archivedOrderRepository.findById(sellingOrder.getId())
                .orElseGet(() -> modelMapper.map(sellingOrder, ArchivedOrder.class));
        archivedBuyingOrder.setRemainingAmount(buyingOrder.getRemainingAmount());
        archivedBuyingOrder.setDateClosing(buyingOrder.getDateClosing());
        archivedSellingOrder.setRemainingAmount(sellingOrder.getRemainingAmount());
        archivedSellingOrder.setDateClosing(sellingOrder.getDateClosing());
        transactionRepository.save(Transaction.builder()
                .date(Optional.ofNullable(buyingOrder.getDateClosing())
                        .orElseGet(sellingOrder::getDateClosing))
                .amount(amount)
                .unitPrice(pricePerUnit)
                .buyingOrder(archivedOrderRepository.save(archivedBuyingOrder))
                .sellingOrder(archivedOrderRepository.save(archivedSellingOrder))
                .build());
        updateOrder(buyingOrder);
        updateOrder(sellingOrder);
    }

    private void updateOrder(Order order) {
        if (order.getRemainingAmount() == 0) {
            orderRepository.deleteById(order.getId());
        } else {
            orderRepository.save(order);
        }
    }

}
