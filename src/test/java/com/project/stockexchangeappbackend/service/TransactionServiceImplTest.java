package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.*;
import com.project.stockexchangeappbackend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static com.project.stockexchangeappbackend.service.OrderServiceImplTest.createCustomArchivedOrder;
import static com.project.stockexchangeappbackend.service.OrderServiceImplTest.createCustomOrder;
import static com.project.stockexchangeappbackend.service.ResourceServiceImplTest.createCustomResource;
import static com.project.stockexchangeappbackend.service.StockServiceImplTest.createCustomStock;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.createCustomUser;
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
    AllOrdersRepository allOrdersRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ResourceRepository resourceRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ModelMapper modelMapper;

    @Test
    void shouldReturnTransactionById() {
        long id = 1L;
        Transaction transaction = createCustomTransaction(id, 100, OffsetDateTime.now(),
                null, null, BigDecimal.ONE);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        assertTransaction(transactionService.findTransactionById(id), transaction);
    }

    @Test
    void shouldPageAndFilterTransactions() {
        Stock stock = createCustomStock(1L, "WIG30", "W30", 1024, BigDecimal.TEN);
        User user1 = createCustomUser(1L, "test1@test.pl", "John", "Kowal", BigDecimal.ZERO);
        User user2 = createCustomUser(2L, "test2@test.pl", "Bobby", "Lawok", BigDecimal.ZERO);
        Order order1 = createCustomOrder(1L, 100, 0, OrderType.BUYING_ORDER, PriceType.EQUAL,
                BigDecimal.ONE, OffsetDateTime.now(), OffsetDateTime.now().minusHours(2), null, user1, stock);
        Order order2 = createCustomOrder(2L, 100, 0, OrderType.SELLING_ORDER, PriceType.EQUAL,
                BigDecimal.ONE, OffsetDateTime.now().minusHours(2), OffsetDateTime.now().minusHours(3), null, user2, stock);

        ArchivedOrder buyingOrder = createCustomArchivedOrder(order1);
        ArchivedOrder sellingOrder = createCustomArchivedOrder(order2);

        Transaction transaction1 = createCustomTransaction(1, 50, OffsetDateTime.now(),
                buyingOrder, sellingOrder, buyingOrder.getPrice());
        Transaction transaction2 = createCustomTransaction(1, 50, OffsetDateTime.now(),
                buyingOrder, sellingOrder, buyingOrder.getPrice());

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (Specification<Transaction>) (root, criteriaQuery, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("id"), 1);
        when(transactionRepository.findAll(transactionSpecification, pageable))
                .thenReturn(new PageImpl<>(transactions, pageable, transactions.size()));
        Page<Transaction> output = transactionService.findAllTransactions(pageable, transactionSpecification);
        assertEquals(transactions.size(), output.getNumberOfElements());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGettingTransactionById() {
        long id = 1L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> transactionService.findTransactionById(id));
    }

    @Test
    void shouldMakeTransaction() {
        User seller = createCustomUser(1L, "seller@test.com", "John", "Kowal", BigDecimal.ZERO);
        User buyer = createCustomUser(2L, "buyer@test.com", "John", "Kowal", BigDecimal.ZERO);
        Stock stock = createCustomStock(1L, "WiG20", "W20", 1024, BigDecimal.TEN);
        Order sellingOrder = createCustomOrder(1L, 100, 100, OrderType.SELLING_ORDER,
                PriceType.GREATER_OR_EQUAL, BigDecimal.TEN, OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(1), null, seller, stock);
        Order buyingOrder = createCustomOrder(2L, 100, 80, OrderType.BUYING_ORDER,
                PriceType.LESS_OR_EQUAL, BigDecimal.valueOf(12), OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(1), null, buyer, stock);
        ArchivedOrder archivedBuyingOrder = createCustomArchivedOrder(buyingOrder);
        Resource sellerResource = createCustomResource(1L, stock, seller, sellingOrder.getAmount());
        Resource buyerResource = createCustomResource(2L, stock, buyer, buyingOrder.getAmount() - buyingOrder.getRemainingAmount());

        when(archivedOrderRepository.findById(buyingOrder.getId())).thenReturn(Optional.of(archivedBuyingOrder));
        when(archivedOrderRepository.findById(sellingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(sellingOrder, ArchivedOrder.class)).thenReturn(createCustomArchivedOrder(sellingOrder));
        when(resourceRepository.findByUserAndStock(seller, stock)).thenReturn(Optional.of(sellerResource));
        when(resourceRepository.findByUserAndStock(buyer, stock)).thenReturn(Optional.of(buyerResource));

        assertAll(() -> transactionService.makeTransaction(buyingOrder, sellingOrder, buyingOrder.getRemainingAmount(), sellingOrder.getPrice()));
    }

    @Test
    void shouldMakeTransactionWhenBuyerNotOwnBuyingStock() {
        User seller = createCustomUser(1L, "seller@test.com", "John", "Kowal", BigDecimal.ZERO);
        User buyer = createCustomUser(2L, "buyer@test.com", "John", "Kowal", BigDecimal.ZERO);
        Stock stock = createCustomStock(1L, "WiG20", "W20", 1024, BigDecimal.TEN);
        Order sellingOrder = createCustomOrder(1L, 100, 100, OrderType.SELLING_ORDER,
                PriceType.GREATER_OR_EQUAL, BigDecimal.TEN, OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(1), null, seller, stock);
        Order buyingOrder = createCustomOrder(2L, 100, 100, OrderType.BUYING_ORDER,
                PriceType.LESS_OR_EQUAL, BigDecimal.valueOf(12), OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(1), null, buyer, stock);
        Resource sellerResource = createCustomResource(1L, stock, seller, sellingOrder.getAmount());

        when(archivedOrderRepository.findById(buyingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(buyingOrder, ArchivedOrder.class)).thenReturn(createCustomArchivedOrder(buyingOrder));
        when(archivedOrderRepository.findById(sellingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(sellingOrder, ArchivedOrder.class)).thenReturn(createCustomArchivedOrder(sellingOrder));
        when(resourceRepository.findByUserAndStock(seller, stock)).thenReturn(Optional.of(sellerResource));
        when(resourceRepository.findByUserAndStock(buyer, stock)).thenReturn(Optional.empty());
        when(userRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));
        assertAll(() -> transactionService.makeTransaction(buyingOrder, sellingOrder, buyingOrder.getAmount(), sellingOrder.getPrice()));
    }

    @Test
    void shouldThrowEntityNotFoundWhenMakingTransactionAndUserNotExist() {
        User seller = createCustomUser(1L, "seller@test.com", "John", "Kowal", BigDecimal.ZERO);
        User buyer = createCustomUser(2L, "buyer@test.com", "John", "Kowal", BigDecimal.ZERO);
        Stock stock = createCustomStock(1L, "WiG20", "W20", 1024, BigDecimal.TEN);
        Order sellingOrder = createCustomOrder(1L, 100, 100, OrderType.SELLING_ORDER,
                PriceType.GREATER_OR_EQUAL, BigDecimal.TEN, OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(1), null, seller, stock);
        Order buyingOrder = createCustomOrder(2L, 100, 100, OrderType.BUYING_ORDER,
                PriceType.LESS_OR_EQUAL, BigDecimal.valueOf(12), OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(1), null, buyer, stock);
        Resource sellerResource = createCustomResource(1L, stock, seller, sellingOrder.getAmount());

        when(archivedOrderRepository.findById(buyingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(buyingOrder, ArchivedOrder.class)).thenReturn(createCustomArchivedOrder(buyingOrder));
        when(archivedOrderRepository.findById(sellingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(sellingOrder, ArchivedOrder.class)).thenReturn(createCustomArchivedOrder(sellingOrder));
        when(resourceRepository.findByUserAndStock(seller, stock)).thenReturn(Optional.of(sellerResource));
        when(resourceRepository.findByUserAndStock(buyer, stock)).thenReturn(Optional.empty());
        when(userRepository.findById(buyer.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> transactionService.makeTransaction(buyingOrder, sellingOrder, buyingOrder.getAmount(), sellingOrder.getPrice()));
    }

    @Test
    void shouldThrowEntityNotFoundWhenMakingTransactionAndSellerNotOwnStock() {
        User seller = createCustomUser(1L, "seller@test.com", "John", "Kowal", BigDecimal.ZERO);
        User buyer = createCustomUser(2L, "buyer@test.com", "John", "Kowal", BigDecimal.ZERO);
        Stock stock = createCustomStock(1L, "WiG20", "W20", 1024, BigDecimal.TEN);
        Order sellingOrder = createCustomOrder(1L, 100, 100, OrderType.SELLING_ORDER,
                PriceType.GREATER_OR_EQUAL, BigDecimal.TEN, OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(1), null, seller, stock);
        Order buyingOrder = createCustomOrder(2L, 100, 100, OrderType.BUYING_ORDER,
                PriceType.LESS_OR_EQUAL, BigDecimal.valueOf(12), OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(1), null, buyer, stock);

        when(archivedOrderRepository.findById(buyingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(buyingOrder, ArchivedOrder.class)).thenReturn(createCustomArchivedOrder(buyingOrder));
        when(archivedOrderRepository.findById(sellingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(sellingOrder, ArchivedOrder.class)).thenReturn(createCustomArchivedOrder(sellingOrder));
        when(resourceRepository.findByUserAndStock(seller, stock)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> transactionService.makeTransaction(buyingOrder, sellingOrder, buyingOrder.getAmount(), sellingOrder.getPrice()));
    }

    @Test
    void shouldReturnTransactionByStockIdForPricing() {
        Stock stock = createCustomStock(1L, "WIG30", "W30", 1024, BigDecimal.TEN);
        User user1 = createCustomUser(1L, "test1@test.pl", "John", "Kowal", BigDecimal.ZERO);
        Order order1 = createCustomOrder(1L, stock.getAmount(), 0, OrderType.BUYING_ORDER, PriceType.EQUAL,
                BigDecimal.ONE, OffsetDateTime.now(), OffsetDateTime.now().minusHours(2), null, user1, stock);
        ArchivedOrder buyingOrder = createCustomArchivedOrder(order1);
        Transaction transaction1 = createCustomTransaction(1, stock.getAmount(), OffsetDateTime.now().plusDays(1),
                buyingOrder, null, buyingOrder.getPrice());
        Transaction transaction2 = createCustomTransaction(2, 50, OffsetDateTime.now(),
                buyingOrder, null, buyingOrder.getPrice());
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        List<Transaction> expected = Collections.singletonList(transaction1);
        when(transactionService.getTransactionsByStockIdForPricing(stock.getId(), stock.getAmount()))
                .thenReturn(transactions);
        List<Transaction> output = transactionService.getTransactionsByStockIdForPricing(stock.getId(), stock.getAmount());
        assertEquals(expected.size(), output.size());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(expected.get(i), output.get(i));
        }

    }

    public static Transaction createCustomTransaction(long id, int amount, OffsetDateTime date, ArchivedOrder buyingOrder,
                                                      ArchivedOrder sellingOrder, BigDecimal price) {
        return Transaction.builder()
                .id(id).amount(amount).date(date)
                .sellingOrder(sellingOrder).buyingOrder(buyingOrder)
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
