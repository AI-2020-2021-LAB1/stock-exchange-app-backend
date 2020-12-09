package com.project.stockexchangeappbackend.scheduler;

import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.StockIndexValue;
import com.project.stockexchangeappbackend.service.StockIndexValueService;
import com.project.stockexchangeappbackend.service.StockService;
import com.project.stockexchangeappbackend.util.StockIndexTimeProperties;
import com.project.stockexchangeappbackend.util.ThreadsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.project.stockexchangeappbackend.service.StockServiceImplTest.getStocksList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockPriceChangeRatioSchedulerTest {

    @InjectMocks
    StockPriceChangeRatioScheduler stockPriceChangeRatioScheduler;

    @Mock
    StockService stockService;

    @Mock
    StockIndexValueService stockIndexValueService;

    @Mock
    StockIndexTimeProperties stockIndexTimeProperties;

    @Mock
    ThreadsProperties threadsProperties;

    @Test
    @DisplayName("Fixing stock price change ratio")
    void testScheduler() {
        List<Stock> stockList = getStocksList();
        List<StockIndexValue> stockIndexValues = stockList.stream()
                .map(stock -> StockIndexValue.builder()
                        .id(stock.getId())
                        .timestamp(OffsetDateTime.now().minusMinutes(1))
                        .value(stock.getCurrentPrice().add(BigDecimal.ONE))
                        .build())
                .collect(Collectors.toList());

        when(stockService.getAllStocks()).thenReturn(stockList);
        when(threadsProperties.getStockProcessing()).thenReturn(1);
        when(stockIndexTimeProperties.getStockPriceChangeRatioPeriod()).thenReturn(60000);
        when(stockIndexValueService.getFirstStockIndexValueBeforeMinutesAgo(stockList.get(0), 1))
                .thenReturn(Optional.of(stockIndexValues.get(0)));
        when(stockIndexValueService.getFirstStockIndexValueBeforeMinutesAgo(stockList.get(1), 1))
                .thenReturn(Optional.of(stockIndexValues.get(1)));
        assertAll(() -> stockPriceChangeRatioScheduler.run());
    }

    @Test
    @DisplayName("Fixing stock price change ratio when old price is zero")
    void testSchedulerWhenOldPriceIsZero() {
        List<Stock> stockList = getStocksList();
        List<StockIndexValue> stockIndexValues = stockList.stream()
                .map(stock -> StockIndexValue.builder()
                        .id(stock.getId())
                        .timestamp(OffsetDateTime.now().minusMinutes(1))
                        .value(BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());

        when(stockService.getAllStocks()).thenReturn(stockList);
        when(threadsProperties.getStockProcessing()).thenReturn(1);
        when(stockIndexTimeProperties.getStockPriceChangeRatioPeriod()).thenReturn(60000);
        when(stockIndexValueService.getFirstStockIndexValueBeforeMinutesAgo(stockList.get(0), 1))
                .thenReturn(Optional.of(stockIndexValues.get(0)));
        when(stockIndexValueService.getFirstStockIndexValueBeforeMinutesAgo(stockList.get(1), 1))
                .thenReturn(Optional.of(stockIndexValues.get(1)));
        assertAll(() -> stockPriceChangeRatioScheduler.run());
    }

}