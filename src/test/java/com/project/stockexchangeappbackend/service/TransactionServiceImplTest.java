package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.ArchivedOrder;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.Transaction;
import com.project.stockexchangeappbackend.repository.ArchivedOrderRepository;
import com.project.stockexchangeappbackend.repository.OrderRepository;
import com.project.stockexchangeappbackend.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @InjectMocks
    TransactionServiceImpl transactionService;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    ArchivedOrderRepository archivedOrderRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ModelMapper modelMapper;

    @Test
    void shouldReturnTransactionById() {
        long id = 1L;
        Transaction transaction = createCustomTransaction(id, 100, OffsetDateTime.now(), null, null, BigDecimal.ONE);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        assertTransaction(transactionService.findTransactionById(id), transaction);
    }


    @Test
    void shouldThrowNotFoundExceptionWhenGettingTransactionById() {
        long id = 1L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> transactionService.findTransactionById(id));
    }

    public static Transaction createCustomTransaction (long id, int amount, OffsetDateTime date, ArchivedOrder buyingOffer,
                                                       ArchivedOrder sellingOrder, BigDecimal price) {
        return Transaction.builder()
                .id(id).amount(amount).date(date)
                .sellingOrder(sellingOrder).buyingOrder(buyingOffer)
                .unitPrice(price)
                .build();
    }

    public static void assertTransaction(Transaction output, Transaction expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertEquals(expected.getDate(), output.getDate()),
                () -> assertEquals(expected.getUnitPrice(), output.getUnitPrice()),
                () -> assertEquals(expected.getBuyingOrder(), output.getBuyingOrder()),
                () -> assertEquals(expected.getSellingOrder(), output.getSellingOrder()));
    }

}
