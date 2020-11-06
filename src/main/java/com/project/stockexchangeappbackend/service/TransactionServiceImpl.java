package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.ArchivedOrder;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.Resource;
import com.project.stockexchangeappbackend.entity.Transaction;
import com.project.stockexchangeappbackend.repository.*;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction Not Found"));
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional
    public void makeTransaction(Order buyingOrder, Order sellingOrder, int amount, BigDecimal pricePerUnit) {
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
                .buyingOrder(archivedBuyingOrder)
                .sellingOrder(archivedSellingOrder)
                .build());
        updateOrder(buyingOrder);
        updateOrder(sellingOrder);
        exchangeMoneyAndStocks(buyingOrder, sellingOrder, amount, pricePerUnit);
    }

    @Override
    public Page<Transaction> findAllTransactions(Pageable pageable, Specification<Transaction> specification) {
        return transactionRepository.findAll(specification, pageable);
    }

    private void updateOrder(Order order) {
        if (order.getRemainingAmount() == 0) {
            orderRepository.deleteById(order.getId());
        } else {
            orderRepository.save(order);
        }
    }

    private void exchangeMoneyAndStocks(Order buyingOrder, Order sellingOrder, int amount, BigDecimal pricePerUnit) {
        Resource sellerResource = resourceRepository.findByUserAndStock(sellingOrder.getUser(), sellingOrder.getStock())
                .orElseThrow(() -> new EntityNotFoundException("Data in database not consistent."));

        Resource buyerResource = resourceRepository.findByUserAndStock(buyingOrder.getUser(), buyingOrder.getStock())
                .orElseGet(() -> Resource.builder()
                        .amount(0)
                        .stock(buyingOrder.getStock())
                        .user(userRepository.findById(buyingOrder.getUser().getId())
                                .orElseThrow(() -> new EntityNotFoundException("Data in database not consistent.")))
                        .build());
        sellerResource.setAmount(sellerResource.getAmount() - amount);
        sellerResource.getUser().setMoney(sellerResource.getUser().getMoney()
                .add(pricePerUnit.multiply(BigDecimal.valueOf(amount))));
        buyerResource.setAmount(buyerResource.getAmount() + amount);
        buyerResource.getUser().setMoney(buyerResource.getUser().getMoney()
                .subtract(pricePerUnit.multiply(BigDecimal.valueOf(amount))));
        resourceRepository.save(sellerResource);
        resourceRepository.save(buyerResource);
    }

}
