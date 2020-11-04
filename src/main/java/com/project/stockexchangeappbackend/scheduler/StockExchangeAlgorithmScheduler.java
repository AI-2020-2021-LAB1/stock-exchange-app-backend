package com.project.stockexchangeappbackend.scheduler;

import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.PriceType;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.service.OrderService;
import com.project.stockexchangeappbackend.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class StockExchangeAlgorithmScheduler {

    private final OrderService orderService;
    private final TransactionService transactionService;

    @Scheduled(fixedDelay = 1000)
    public void run() {
        log.info("Stock exchange algorithm started.");
        long start = System.nanoTime();
        List<Order> activeBuyingOrder = orderService.getActiveBuyingOrders();
        ConcurrentMap<Stock, List<Order>> groupedBuyingOrdersByStock = activeBuyingOrder.parallelStream()
                .collect(Collectors.groupingByConcurrent(Order::getStock));
        groupedBuyingOrdersByStock.forEach((k,v) ->
                groupedBuyingOrdersByStock.put(k, v.stream()
                .sorted(Comparator.comparing(Order::getPrice))
                .collect(Collectors.toList())));

        ConcurrentMap<Stock, List<Order>> groupedAndSortedSellingOrders =  groupedBuyingOrdersByStock.entrySet()
                .parallelStream()
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, entry -> {
                    List<Order> relatedList = groupedBuyingOrdersByStock.get(entry.getKey());
                    return orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(entry.getKey(),
                            relatedList.get(relatedList.size() - 1).getPrice());
                }));

        groupedBuyingOrdersByStock.entrySet().parallelStream()
                .forEach(entry -> {
                    List<Order> buyingOrders = entry.getValue();
                    List<Order> sellingOrders = groupedAndSortedSellingOrders.get(entry.getKey());
                    while (!(buyingOrders.isEmpty() || sellingOrders.isEmpty())) {
                        Order buyingOrder = buyingOrders.get(0);
                        Order sellingOrder = sellingOrders.get(0);
                        if (checkOrderCompatibility(buyingOrder, sellingOrder)) {
                            BigDecimal transactionPrice = buyingOrder.getPriceType() == PriceType.EQUAL ?
                                    buyingOrder.getPrice() : sellingOrder.getPrice();
                            OffsetDateTime transactionTime = OffsetDateTime.now(ZoneId.systemDefault());
                            int transactionAmount = Math.min(buyingOrder.getRemainingAmount(), sellingOrder.getRemainingAmount());
                            buyingOrder.setRemainingAmount(buyingOrder.getRemainingAmount() - transactionAmount);
                            sellingOrder.setRemainingAmount(sellingOrder.getRemainingAmount() - transactionAmount);
                            if (buyingOrder.getRemainingAmount() == 0) {
                                buyingOrder.setDateClosing(transactionTime);
                            }
                            if (sellingOrder.getRemainingAmount() == 0) {
                                sellingOrder.setDateClosing(transactionTime);
                            }
                            transactionService.createTransaction(buyingOrder, sellingOrder, transactionAmount, transactionPrice);
                            if (buyingOrder.getRemainingAmount() == 0) {
                                buyingOrders.remove(buyingOrder);
                            }
                            if (sellingOrder.getRemainingAmount() == 0) {
                                sellingOrders.remove(sellingOrder);
                            }
                        } else {
                            // TODO: when buyer == seller
                            buyingOrders.remove(buyingOrder);
                        }
                    }
                });
        long stop = (System.nanoTime() - start) / 1000000;
        log.info("Stock exchange algorithm stopped. Execution time: " + stop);
    }

    private boolean checkOrderCompatibility(Order buyingOrder, Order sellingOrder) {
        return !buyingOrder.getUser().getId().equals(sellingOrder.getUser().getId())
                && buyingOrder.getPrice().compareTo(sellingOrder.getPrice()) >= 0
                && ((buyingOrder.getPriceType() != PriceType.EQUAL || sellingOrder.getPriceType() != PriceType.EQUAL)
                    || buyingOrder.getPrice().equals(sellingOrder.getPrice()));
    }

}
