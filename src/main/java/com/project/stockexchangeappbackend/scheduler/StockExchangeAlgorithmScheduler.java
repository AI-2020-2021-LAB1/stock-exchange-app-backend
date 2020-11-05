package com.project.stockexchangeappbackend.scheduler;

import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.PriceType;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.service.OrderService;
import com.project.stockexchangeappbackend.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class StockExchangeAlgorithmScheduler {

    private final OrderService orderService;
    private final TransactionService transactionService;

    @Scheduled(fixedDelayString = "${application.stock-algorithm.delay-time}")
    public void run() {
        removeInactiveOrders();
        executeStockAlgorithm();
    }

    private void removeInactiveOrders() {
        log.info("Movement inactive orders started.");
        long start = System.nanoTime();
        orderService.moveInactiveOrders();
        long stop = (System.nanoTime() - start) / 1000000;
        log.info("Movement inactive orders finished. Execution time: " + stop + " ms.");
    }

    private void executeStockAlgorithm() {
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
                    int index = 0;
                    while (!(buyingOrders.isEmpty() || sellingOrders.isEmpty())) {
                        Order buyingOrder = buyingOrders.get(index);
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
                                buyingOrders.remove(buyingOrder);
                            }
                            if (sellingOrder.getRemainingAmount() == 0) {
                                sellingOrder.setDateClosing(transactionTime);
                                sellingOrders.remove(sellingOrder);
                                index = 0;
                            }
                            transactionService.makeTransaction(buyingOrder, sellingOrder, transactionAmount, transactionPrice);
                        } else {
                            if (index == buyingOrders.size() - 1) {
                                sellingOrders.remove(sellingOrder);
                                index = 0;
                            } else if (!checkDifferentUsersRule(buyingOrder.getUser(), sellingOrder.getUser())
                                    || !checkEqualPriceTypeRule(buyingOrder, sellingOrder)) {
                                index++;
                            } else {
                                buyingOrders.remove(buyingOrder);
                                index = 0;
                            }
                        }
                    }
                });
        long stop = (System.nanoTime() - start) / 1000000;
        log.info("Stock exchange algorithm stopped. Execution time: " + stop + " ms.");
    }

    private boolean checkOrderCompatibility(Order buyingOrder, Order sellingOrder) {
        return buyingOrder.getPrice().compareTo(sellingOrder.getPrice()) >= 0
                && checkDifferentUsersRule(buyingOrder.getUser(), sellingOrder.getUser())
                && checkEqualPriceTypeRule(buyingOrder, sellingOrder);
    }

    private boolean checkDifferentUsersRule(User buyer, User seller) {
        return !buyer.getId().equals(seller.getId());
    }

    private boolean checkEqualPriceTypeRule(Order buyingOrder, Order sellingOrder) {
        return ((buyingOrder.getPriceType() != PriceType.EQUAL || sellingOrder.getPriceType() != PriceType.EQUAL)
                || buyingOrder.getPrice().equals(sellingOrder.getPrice()));
    }

}
