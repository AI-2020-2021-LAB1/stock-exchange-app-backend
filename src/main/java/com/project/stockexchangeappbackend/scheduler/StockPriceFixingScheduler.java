package com.project.stockexchangeappbackend.scheduler;

import com.project.stockexchangeappbackend.entity.Transaction;
import com.project.stockexchangeappbackend.service.StockService;
import com.project.stockexchangeappbackend.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Slf4j
@AllArgsConstructor
public class StockPriceFixingScheduler {

    private final StockService stockService;
    private final TransactionService transactionService;


    @Scheduled(fixedDelayString = "${application.stock.fixing-price-cycle}")
    public void run() {
        log.info("Stocks' price fixing started.");
        long start = System.nanoTime();
        stockService.getAllStocks()
                .parallelStream()
                .forEach(stock -> {
                    BigDecimal newPrice = BigDecimal.valueOf(
                            transactionService.getTransactionsByStockIdForPricing(stock.getId(), stock.getAmount())
                            .stream()
                            .mapToInt(Transaction::getAmount)
                            .average()
                            .orElseGet(() -> stock.getCurrentPrice().doubleValue()));
                    double priceChangeRatio = stock.getCurrentPrice().doubleValue() <= 0.00001 ?
                            0.0d : newPrice.subtract(stock.getCurrentPrice())
                            .divide(stock.getCurrentPrice(), RoundingMode.CEILING)
                            .doubleValue();
                    stock.setPriceChangeRatio(priceChangeRatio);
                    stock.setCurrentPrice(newPrice);
                    stockService.updateStock(stock);
                });
        long stop = (System.nanoTime() - start) / 1000000;
        log.info("Stocks' price fixing stopped. Execution time: " + stop + " ms.");
    }

}
