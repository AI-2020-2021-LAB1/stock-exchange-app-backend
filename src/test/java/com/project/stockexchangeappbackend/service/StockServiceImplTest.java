package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.StockDTO;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @InjectMocks
    StockServiceImpl stockService;

    @Mock
    StockRepository stockRepository;

    @Test
    void shouldReturnStockById() {
        Long id = 1L;
        Stock stock = createCustomStock(id, "WIG30", "WIG", 10000, BigDecimal.valueOf(100.20));
        when(stockRepository.findById(id)).thenReturn(Optional.of(stock));
        assertStock(stockService.getStockById(id), stock);
    }

    @Test
    void shouldThrowEntityNotFoundWhenGettingStockById() {
        Long id = 1L;
        when(stockRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> stockService.getStockById(id));
    }

    @Test
    void shouldPageAndFilterStocks() {
        List<Stock> stocks = Arrays.asList(
                createCustomStock(1L, "WIG30", "W30", 10000, BigDecimal.valueOf(100.20)),
                createCustomStock(2L, "WIG20", "W20", 10000, BigDecimal.valueOf(10.20)));
        Pageable pageable = PageRequest.of(0,20);
        Specification<Stock> stockSpecification =
                (Specification<Stock>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");
        when(stockRepository.findAll(stockSpecification, pageable))
                .thenReturn(new PageImpl<>(stocks, pageable, stocks.size()));
        Page<Stock> output = stockService.getStocks(pageable, stockSpecification);
        assertEquals(stocks.size(), output.getNumberOfElements());
        for (int i=0; i<stocks.size(); i++) {
            assertStock(output.getContent().get(i), stocks.get(i));
        }

    }

    public static void assertStock(Stock output, Stock expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getName(), output.getName()),
                () -> assertEquals(expected.getAbbreviation(), output.getAbbreviation()),
                () -> assertEquals(expected.getCurrentPrice(), output.getCurrentPrice()),
                () -> assertEquals(expected.getAmount(), output.getAmount()));
    }

    public static Stock createCustomStock (Long id, String name, String abbreviation,
                                            Integer amount, BigDecimal currentPrice) {
        return Stock.builder()
                .id(id).name(name).abbreviation(abbreviation)
                .amount(amount).currentPrice(currentPrice)
                .build();
    }

    public static StockDTO createCustomStockDTO (Long id, String name, String abbreviation,
                                                 Integer amount, BigDecimal currentPrice) {
        return StockDTO.builder()
                .id(id).name(name).abbreviation(abbreviation)
                .amount(amount).currentPrice(currentPrice)
                .build();
    }

}
