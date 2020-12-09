package com.project.stockexchangeappbackend.scheduler;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.StockIndexValue;
import com.project.stockexchangeappbackend.service.StockIndexValueService;
import com.project.stockexchangeappbackend.service.StockService;
import com.project.stockexchangeappbackend.util.StockIndexTimeProperties;
import com.project.stockexchangeappbackend.util.ThreadsProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Component
@Slf4j
@AllArgsConstructor
public class StockPriceChangeRatioScheduler {

    private final StockService stockService;
    private final StockIndexValueService stockIndexValueService;
    private final StockIndexTimeProperties stockIndexTimeProperties;
    private final ThreadsProperties threadsProperties;

    @Scheduled(initialDelayString = "${application.stock.fixingPriceCycle}",
            fixedRateString = "${application.stock.stockPriceChangeRatioPeriod}")
    public void run() throws InterruptedException {
        log.info("Stocks' price change ratio fixing started.");
        long start = System.nanoTime();
        List<Stock> stocks = stockService.getAllStocks();
        final Semaphore semaphore = new Semaphore(threadsProperties.getStockProcessing());
        List<Runnable> threads = stocks.stream()
                .map(stock -> (Runnable) () -> {
                    Optional<StockIndexValue> stockIndexValue =
                            stockIndexValueService.getFirstStockIndexValueBeforeMinutesAgo(stock,
                                    stockIndexTimeProperties.getStockPriceChangeRatioPeriod()/60000);
                    if (stockIndexValue.isPresent() && !stockIndexValue.get().getValue().equals(BigDecimal.ZERO)) {
                        double priceChangeRatio = stock.getCurrentPrice().subtract(stockIndexValue.get().getValue())
                                .divide(stockIndexValue.get().getValue(), RoundingMode.CEILING)
                                .doubleValue();
                        stock.setPriceChangeRatio(priceChangeRatio);
                    }
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
        long stop = (System.nanoTime() - start) / 1000000;
        log.info("Stocks' price change ratio fixing fixing stopped. Execution time: " + stop + " ms.");
    }

}
