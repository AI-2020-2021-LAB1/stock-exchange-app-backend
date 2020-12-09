package com.project.stockexchangeappbackend.scheduler;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.StockIndexValue;
import com.project.stockexchangeappbackend.service.StockIndexValueService;
import com.project.stockexchangeappbackend.service.StockService;
import com.project.stockexchangeappbackend.service.TransactionService;
import com.project.stockexchangeappbackend.util.ThreadsProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class StockPriceFixingScheduler {

    private final StockService stockService;
    private final TransactionService transactionService;
    private final StockIndexValueService stockIndexValueService;
    private final ThreadsProperties threadsProperties;

    @Scheduled(fixedDelayString = "${application.stock.fixingPriceCycle}")
    public void run() throws InterruptedException {
        log.info("Stocks' price fixing started.");
        long start = System.nanoTime();

        OffsetDateTime timestamp = OffsetDateTime.now(ZoneId.systemDefault());
        List<Stock> stocks = stockService.getAllStocks();
        Collection<StockIndexValue> stockIndexValues = new LinkedBlockingQueue<>();
        final Semaphore semaphore = new Semaphore(threadsProperties.getStockProcessing());
        List<Runnable> threads = stocks.stream()
                .map(stock -> (Runnable) () -> {
                    BigDecimal newPrice = BigDecimal.valueOf(
                            transactionService.getTransactionsByStockIdForPricing(stock.getId(), stock.getAmount())
                                    .stream()
                                    .mapToDouble(transaction -> transaction.getUnitPrice().doubleValue())
                                    .average()
                                    .orElseGet(() -> stock.getCurrentPrice().doubleValue()));
                    stock.setCurrentPrice(newPrice);
                    stockIndexValues.add(StockIndexValue.builder()
                            .timestamp(timestamp)
                            .value(newPrice)
                            .stock(stock)
                            .build());
                    semaphore.release();
                }).collect(Collectors.toList());
        for (Runnable thread : threads) {
            semaphore.acquire();
            thread.run();
        }
        for (int i=0; i<threadsProperties.getStockProcessing(); i++) {
            semaphore.acquire();
        }
        stockService.updateStocks(stocks);
        stockIndexValueService.appendValues(stockIndexValues);
        long stop = (System.nanoTime() - start) / 1000000;
        log.info("Stocks' price fixing stopped. Execution time: " + stop + " ms.");
    }

}
