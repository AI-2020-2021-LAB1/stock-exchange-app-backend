package com.project.stockexchangeappbackend.scheduler;

import com.google.common.collect.Lists;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.PriceType;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.service.OrderService;
import com.project.stockexchangeappbackend.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.project.stockexchangeappbackend.service.OrderServiceImplTest.createBuyingOrder;
import static com.project.stockexchangeappbackend.service.OrderServiceImplTest.createSellingOrder;
import static com.project.stockexchangeappbackend.service.StockServiceImplTest.getStocksList;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.getUsersList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockExchangeAlgorithmSchedulerTest {

    @InjectMocks
    StockExchangeAlgorithmScheduler stockExchangeAlgorithmScheduler;

    @Mock
    OrderService orderService;

    @Mock
    TransactionService transactionService;

    @Test
    @DisplayName("Exchange algorithm")
    void testScheduler() {
        List<Stock> stockList = getStocksList();
        List<Order> activeBuyingOrders = stockList.stream()
                .map(stock -> createBuyingOrder(1L, stock.getAmount(), stock.getCurrentPrice(),
                        OffsetDateTime.now().plusHours(1), getUsersList().get(0), stock)).collect(Collectors.toList());
        List<List<Order>> activeSellingOrders = activeBuyingOrders.stream()
                .map(order -> new ArrayList<>(Collections.singleton(
                        createSellingOrder(2L, order.getAmount(), order.getPrice(), order.getDateExpiration(),
                                getUsersList().get(2), order.getStock()))))
                .collect(Collectors.toList());

        when(orderService.getActiveBuyingOrders()).thenReturn(activeBuyingOrders);
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(0)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(0));
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(1)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(1));
        assertAll(() -> stockExchangeAlgorithmScheduler.run());
    }

    @Test
    @DisplayName("Exchange algorithm - omit buying order when buyer is seller")
    void testSchedulerOmitOneBuyingOrderTheSameBuyerAndSeller() {
        List<Stock> stockList = getStocksList();
        List<Order> activeBuyingOrders = stockList.stream()
                .map(stock -> createBuyingOrder(1L, stock.getAmount(), BigDecimal.TEN,
                        OffsetDateTime.now().plusHours(1), getUsersList().get(0), stock)).collect(Collectors.toList());
        List<List<Order>> activeSellingOrders = activeBuyingOrders.stream()
                .map(order -> new ArrayList<>(Collections.singleton(
                        createSellingOrder(2L, order.getAmount()/4, order.getPrice(), order.getDateExpiration(),
                                getUsersList().get(2), order.getStock()))))
                .collect(Collectors.toList());
        activeBuyingOrders.add(
                createBuyingOrder(1L, stockList.get(0).getAmount(), BigDecimal.ONE,
                        OffsetDateTime.now().plusHours(1), getUsersList().get(2), stockList.get(0)));

        when(orderService.getActiveBuyingOrders()).thenReturn(activeBuyingOrders);
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(0)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(0));
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(1)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(1));
        assertAll(() -> stockExchangeAlgorithmScheduler.run());
    }

    @Test
    @DisplayName("Exchange algorithm - omit selling order when buyer is seller")
    void testSchedulerOmitOneSellingOrderTheSameBuyerAndSeller() {
        List<Stock> stockList = getStocksList();
        List<Order> activeBuyingOrders = stockList.stream()
                .map(stock -> createBuyingOrder(1L, stock.getAmount(), stock.getCurrentPrice(),
                        OffsetDateTime.now().plusHours(1), getUsersList().get(0), stock)).collect(Collectors.toList());
        List<List<Order>> activeSellingOrders = activeBuyingOrders.stream()
                .map(order -> new ArrayList<>(Collections.singleton(
                        createSellingOrder(2L, order.getAmount()/4, order.getPrice(), order.getDateExpiration(),
                                getUsersList().get(2), order.getStock()))))
                .collect(Collectors.toList());
        activeSellingOrders.get(0).add(
                createSellingOrder(3L, stockList.get(0).getAmount()/4, stockList.get(0).getCurrentPrice(),
                        OffsetDateTime.now().plusHours(1), getUsersList().get(0), stockList.get(0)));
        activeSellingOrders.set(0, Lists.reverse(activeSellingOrders.get(0)));

        when(orderService.getActiveBuyingOrders()).thenReturn(activeBuyingOrders);
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(0)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(0));
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(1)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(1));
        assertAll(() -> stockExchangeAlgorithmScheduler.run());
    }

    @Test
    @DisplayName("Exchange algorithm - remove buying order when price is too small")
    void testSchedulerRemoveBuyingOrder() {
        List<Stock> stockList = getStocksList();
        List<Order> activeBuyingOrders = stockList.stream()
                .map(stock -> createBuyingOrder(1L, stock.getAmount(), BigDecimal.ONE,
                        OffsetDateTime.now().plusHours(1), getUsersList().get(0), stock)).collect(Collectors.toList());
        List<List<Order>> activeSellingOrders = activeBuyingOrders.stream()
                .map(order -> {
                    var orders = new ArrayList<>(Collections.singleton(
                            createSellingOrder(2L, order.getAmount(), order.getPrice().multiply(BigDecimal.TEN),
                                    order.getDateExpiration(), getUsersList().get(2), order.getStock())));
                    orders.get(0).setPriceType(PriceType.GREATER_OR_EQUAL);
                    return orders;
                })
                .collect(Collectors.toList());
        activeBuyingOrders.add(
                createBuyingOrder(1L, stockList.get(0).getAmount(), BigDecimal.ONE,
                        OffsetDateTime.now().plusHours(1), getUsersList().get(0), stockList.get(0)));

        when(orderService.getActiveBuyingOrders()).thenReturn(activeBuyingOrders);
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(0)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(0));
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(1)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(1));
        assertAll(() -> stockExchangeAlgorithmScheduler.run());
    }

    @Test
    @DisplayName("Exchange algorithm - when cannot perform database operation")
    void testSchedulerWhenCannotPerformDatabaseOperation() {
        List<Stock> stockList = getStocksList();
        List<Order> activeBuyingOrders = stockList.stream()
                .map(stock -> createBuyingOrder(1L, stock.getAmount(), stock.getCurrentPrice(),
                        OffsetDateTime.now().plusHours(1), getUsersList().get(0), stock)).collect(Collectors.toList());
        List<List<Order>> activeSellingOrders = activeBuyingOrders.stream()
                .map(order -> new ArrayList<>(Collections.singleton(
                        createSellingOrder(2L, order.getAmount(), order.getPrice(), order.getDateExpiration(),
                                getUsersList().get(2), order.getStock()))))
                .collect(Collectors.toList());

        when(orderService.getActiveBuyingOrders()).thenReturn(activeBuyingOrders);
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(0)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(0));
        when(orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(eq(stockList.get(1)), any(BigDecimal.class)))
                .thenReturn(activeSellingOrders.get(1));
        doThrow(new DataIntegrityViolationException("Database error"))
                .when(transactionService)
                        .makeTransaction(any(Order.class), any(Order.class), any(Integer.class), any(BigDecimal.class));
        assertAll(() -> stockExchangeAlgorithmScheduler.run());
    }

}