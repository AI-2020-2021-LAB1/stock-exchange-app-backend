package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.StockIndexValueDTO;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.StockIndexValue;
import com.project.stockexchangeappbackend.repository.StockIndexValueRepository;
import com.project.stockexchangeappbackend.repository.StockRepository;
import com.project.stockexchangeappbackend.util.StockIndexTimeProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.project.stockexchangeappbackend.service.StockServiceImplTest.createCustomStock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockIndexValueServiceImplTest {

    @InjectMocks
    StockIndexValueServiceImpl stockIndexValueService;

    @Mock
    StockRepository stockRepository;

    @Mock
    StockIndexValueRepository stockIndexValueRepository;

    @Mock
    StockIndexTimeProperties stockIndexTimeProperties;

    @Test
    void shouldAppendValue() {
        Stock stock = createCustomStock(1L, "WIG20", "W20", 100, BigDecimal.TEN);
        StockIndexValue stockIndexValue = createCustomStockIndexValue(stock, stock.getCurrentPrice(), OffsetDateTime.now());
        when(stockIndexTimeProperties.getMaxPriceHistoryPeriod()).thenReturn(24);
        when(stockIndexTimeProperties.getFixingPriceCycle()).thenReturn(30000);
        when(stockIndexValueRepository.count(Mockito.any(Specification.class))).thenReturn(24L);
        assertAll(() -> stockIndexValueService.appendValue(stockIndexValue));
    }

    @Test
    void shouldAppendValueWhenMaxRecordsExceed() {
        Stock stock = createCustomStock(1L, "WIG20", "W20", 100, BigDecimal.TEN);
        StockIndexValue stockIndexValue = createCustomStockIndexValue(stock, stock.getCurrentPrice(), OffsetDateTime.now());
        StockIndexValue prevStockIndexValue = createCustomStockIndexValue(stock, stock.getCurrentPrice(), OffsetDateTime.now());
        when(stockIndexTimeProperties.getMaxPriceHistoryPeriod()).thenReturn(1);
        when(stockIndexTimeProperties.getFixingPriceCycle()).thenReturn(30000);
        when(stockIndexValueRepository.count(Mockito.any(Specification.class))).thenReturn(121L);
        when(stockIndexValueRepository.findFirstByStockOrderByTimestampAsc(stock))
                .thenReturn(Optional.of(prevStockIndexValue));
        assertAll(() -> stockIndexValueService.appendValue(stockIndexValue));
    }

    @Test
    void shouldReturnStockIndexValues() {
        Long stockId = 1L;
        Integer interval = 1;
        Stock stock = createCustomStock(1L, "WIG20", "W20", 100, BigDecimal.TEN);
        List<StockIndexValue> results = List.of(
                createCustomStockIndexValue(stock, stock.getCurrentPrice(), OffsetDateTime.now()),
                createCustomStockIndexValue(stock, stock.getCurrentPrice(), OffsetDateTime.now()),
                createCustomStockIndexValue(stock, stock.getCurrentPrice(), OffsetDateTime.now().minusMinutes(2)),
                createCustomStockIndexValue(stock, stock.getCurrentPrice(), OffsetDateTime.now().minusMinutes(2)));
        List<StockIndexValueDTO> expected = List.of(
                new StockIndexValueDTO(results.subList(2,3)),
                new StockIndexValueDTO(results.subList(0,1)));

        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(stockIndexValueRepository.findAll(Mockito.any(Specification.class), Mockito.any(Sort.class)))
                .thenReturn(results);
        List<StockIndexValueDTO> output = stockIndexValueService.getStockIndexValues(stockId, null, interval);
        assertEquals(expected.size(), output.size());
        for (int i=0; i<expected.size(); i++) {
            assertStockIndexValueDTO(expected.get(i), output.get(i));
        }
    }

    @Test
    void shouldReturnStockIndexValuesEmptyList() {
        Long stockId = 1L;
        Integer interval = 1;
        Stock stock = createCustomStock(1L, "WIG20", "W20", 100, BigDecimal.TEN);
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(stockIndexValueRepository.findAll(Mockito.any(Specification.class), Mockito.any(Sort.class)))
                .thenReturn(Collections.emptyList());
        assertEquals(0, stockIndexValueService.getStockIndexValues(stockId, null, interval).size());
    }

    @Test
    void shouldThrowExceptionNotFoundWhenGettingStockIndexHistory() {
        Long stockId = 1L;
        Integer interval = 1;
        when(stockRepository.findById(stockId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> stockIndexValueService.getStockIndexValues(stockId, null, interval));
    }

    public void assertStockIndexValueDTO(StockIndexValueDTO expected, StockIndexValueDTO output) {
        assertAll(() -> assertEquals(expected.getTimestamp(), output.getTimestamp()),
                () -> assertEquals(expected.getOpen(), output.getOpen()),
                () -> assertEquals(expected.getMin(), output.getMin()),
                () -> assertEquals(expected.getMax(), output.getMax()),
                () -> assertEquals(expected.getClose(), output.getClose()));
    }

    public StockIndexValue createCustomStockIndexValue(Stock stock, BigDecimal value, OffsetDateTime timestamp) {
        return StockIndexValue.builder()
                .stock(stock)
                .value(value)
                .timestamp(timestamp)
                .build();
    }



}