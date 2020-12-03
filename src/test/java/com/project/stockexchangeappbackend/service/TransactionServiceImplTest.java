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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static com.project.stockexchangeappbackend.service.OrderServiceImplTest.*;
import static com.project.stockexchangeappbackend.service.StockServiceImplTest.getStocksList;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.getUsersList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        Stock stock = getStocksList().get(0);
        User user1 = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        ArchivedOrder order1 =
                createBuyingArchivedOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        Transaction transaction =
                new Transaction(1L, OffsetDateTime.now(), order1.getAmount(), order1.getPrice(), order1, order2);
        Long id = transaction.getId();

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        assertTransaction(transactionService.findTransactionById(id), transaction);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGettingTransactionById() {
        long id = 1L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> transactionService.findTransactionById(id));
    }

    @Test
    void shouldMakeTransaction() {
        User buyer = getUsersList().get(0);
        User seller = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        Order buyingOrder = createBuyingOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), buyer, stock);
        buyingOrder.setRemainingAmount(80);
        ArchivedOrder archivedBuyingOrder = convertOrder(buyingOrder);
        Order sellingOrder = createSellingOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), seller, stock);
        Resource sellerResource = Resource.builder()
                .id(1L).stock(stock).user(seller).amount(sellingOrder.getAmount()*2).build();
        Resource buyerResource = Resource.builder()
                .id(2L).stock(stock).user(buyer).amount(buyingOrder.getAmount()-buyingOrder.getRemainingAmount()).build();
        buyingOrder.setRemainingAmount(0);
        sellingOrder.setRemainingAmount(20);

        when(archivedOrderRepository.findById(buyingOrder.getId())).thenReturn(Optional.of(archivedBuyingOrder));
        when(archivedOrderRepository.findById(sellingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(sellingOrder, ArchivedOrder.class)).thenReturn(convertOrder(sellingOrder));
        when(resourceRepository.findByUserAndStock(seller, stock)).thenReturn(Optional.of(sellerResource));
        when(resourceRepository.findByUserAndStock(buyer, stock)).thenReturn(Optional.of(buyerResource));
        assertAll(() -> transactionService.makeTransaction(buyingOrder, sellingOrder,
                buyingOrder.getRemainingAmount(), sellingOrder.getPrice()));
    }

    @Test
    void shouldMakeTransactionAndSellingOrderShouldBeClosed() {
        User buyer = getUsersList().get(0);
        User seller = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        Order buyingOrder = createBuyingOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), buyer, stock);
        ArchivedOrder archivedBuyingOrder = convertOrder(buyingOrder);
        Order sellingOrder = createSellingOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), seller, stock);
        Resource sellerResource = Resource.builder()
                .id(1L).stock(stock).user(buyer).amount(buyingOrder.getAmount()).build();
        Resource buyerResource = Resource.builder()
                .id(2L).stock(stock).user(seller).amount(sellingOrder.getAmount()).build();
        buyingOrder.setRemainingAmount(0);
        sellingOrder.setRemainingAmount(0);

        when(archivedOrderRepository.findById(buyingOrder.getId())).thenReturn(Optional.of(archivedBuyingOrder));
        when(archivedOrderRepository.findById(sellingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(sellingOrder, ArchivedOrder.class)).thenReturn(convertOrder(sellingOrder));
        when(resourceRepository.findByUserAndStock(seller, stock)).thenReturn(Optional.of(sellerResource));
        when(resourceRepository.findByUserAndStock(buyer, stock)).thenReturn(Optional.of(buyerResource));
        assertAll(() -> transactionService.makeTransaction(buyingOrder, sellingOrder,
                buyingOrder.getRemainingAmount(), sellingOrder.getPrice()));
    }

    @Test
    void shouldMakeTransactionWhenBuyerNotOwnBuyingStock() {
        User buyer = getUsersList().get(0);
        User seller = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        Order buyingOrder = createBuyingOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), buyer, stock);
        Order sellingOrder = createSellingOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), seller, stock);
        Resource sellerResource = Resource.builder()
                .id(1L).stock(stock).user(seller).amount(sellingOrder.getAmount()).build();

        when(archivedOrderRepository.findById(buyingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(buyingOrder, ArchivedOrder.class)).thenReturn(convertOrder(buyingOrder));
        when(archivedOrderRepository.findById(sellingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(sellingOrder, ArchivedOrder.class)).thenReturn(convertOrder(sellingOrder));
        when(resourceRepository.findByUserAndStock(seller, stock)).thenReturn(Optional.of(sellerResource));
        when(resourceRepository.findByUserAndStock(buyer, stock)).thenReturn(Optional.empty());
        when(userRepository.findById(buyer.getId())).thenReturn(Optional.of(buyer));
        assertAll(() -> transactionService.makeTransaction(buyingOrder, sellingOrder,
                buyingOrder.getAmount(), sellingOrder.getPrice()));
    }

    @Test
    void shouldThrowEntityNotFoundWhenMakingTransactionAndUserNotExist() {
        User buyer = getUsersList().get(0);
        User seller = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        Order buyingOrder = createBuyingOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), buyer, stock);
        Order sellingOrder = createSellingOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), seller, stock);
        Resource sellerResource = Resource.builder()
                .id(1L).stock(stock).user(seller).amount(sellingOrder.getAmount()).build();

        when(archivedOrderRepository.findById(buyingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(buyingOrder, ArchivedOrder.class)).thenReturn(convertOrder(buyingOrder));
        when(archivedOrderRepository.findById(sellingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(sellingOrder, ArchivedOrder.class)).thenReturn(convertOrder(sellingOrder));
        when(resourceRepository.findByUserAndStock(seller, stock)).thenReturn(Optional.of(sellerResource));
        when(resourceRepository.findByUserAndStock(buyer, stock)).thenReturn(Optional.empty());
        when(userRepository.findById(buyer.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> transactionService.makeTransaction(buyingOrder, sellingOrder,
                        buyingOrder.getAmount(), sellingOrder.getPrice()));
    }

    @Test
    void shouldThrowEntityNotFoundWhenMakingTransactionAndSellerNotOwnStock() {
        User buyer = getUsersList().get(0);
        User seller = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        Order buyingOrder = createBuyingOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), buyer, stock);
        Order sellingOrder = createSellingOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), seller, stock);

        when(archivedOrderRepository.findById(buyingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(buyingOrder, ArchivedOrder.class)).thenReturn(convertOrder(buyingOrder));
        when(archivedOrderRepository.findById(sellingOrder.getId())).thenReturn(Optional.empty());
        when(modelMapper.map(sellingOrder, ArchivedOrder.class)).thenReturn(convertOrder(sellingOrder));
        when(resourceRepository.findByUserAndStock(seller, stock)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> transactionService.makeTransaction(buyingOrder, sellingOrder,
                        buyingOrder.getAmount(), sellingOrder.getPrice()));
    }

    @Test
    void shouldPageAndFilterTransactions() {
        User user1 = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        ArchivedOrder order1 =
                createBuyingArchivedOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = Arrays.asList(
            new Transaction(1L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2),
            new Transaction(2L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);

        when(transactionRepository.findAll(transactionSpecification, pageable))
                .thenReturn(new PageImpl<>(transactions, pageable, transactions.size()));
        Page<Transaction> output = transactionService.findAllTransactions(pageable, transactionSpecification);
        assertEquals(transactions.size(), output.getNumberOfElements());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldPageAndFilterOwnedTransactionsAllTransactionTypes(@Mock SecurityContext securityContext,
                                                                 @Mock Authentication authentication) {
        User user1 = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        ArchivedOrder order1 =
                createBuyingArchivedOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = Arrays.asList(
            new Transaction(1L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2),
            new Transaction(2L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user1.getEmail());
        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(transactions, pageable, transactions.size()));
        Page<Transaction> output =
                transactionService.getOwnedTransactions(pageable, transactionSpecification, true, true);
        assertEquals(transactions.size(), output.getNumberOfElements());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldPageAndFilterOwnedTransactionsOnlyBuyingOrders(@Mock SecurityContext securityContext,
                                                              @Mock Authentication authentication) {
        User user1 = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        ArchivedOrder order1 =
                createBuyingArchivedOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = Arrays.asList(
            new Transaction(1L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2),
            new Transaction(2L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user1.getEmail());
        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(transactions, pageable, transactions.size()));
        Page<Transaction> output =
                transactionService.getOwnedTransactions(pageable, transactionSpecification, false, true);
        assertEquals(transactions.size(), output.getNumberOfElements());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldPageAndFilterOwnedTransactionsOnlySellingOrders(@Mock SecurityContext securityContext,
                                                               @Mock Authentication authentication) {
        User user1 = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        ArchivedOrder order1 =
                createBuyingArchivedOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = Arrays.asList(
            new Transaction(1L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2),
            new Transaction(2L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user2.getEmail());
        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(transactions, pageable, transactions.size()));
        Page<Transaction> output =
                transactionService.getOwnedTransactions(pageable, transactionSpecification, true, false);
        assertEquals(transactions.size(), output.getNumberOfElements());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldPageAndFilterOwnedTransactionsNoneTransactions(@Mock SecurityContext securityContext,
                                                              @Mock Authentication authentication) {
        User user2 = getUsersList().get(2);
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user2.getEmail());
        Page<Transaction> output =
                transactionService.getOwnedTransactions(pageable, transactionSpecification, false, false);
        assertEquals(0, output.getNumberOfElements());
    }

    @Test
    void shouldPageAndFilterTransactionsByOrder() {
        User user1 = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        AllOrders order =
                createBuyingAllOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order1 =
                createBuyingArchivedOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = Arrays.asList(
            new Transaction(1L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2),
            new Transaction(2L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        Long orderId = order.getId();

        when(allOrdersRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(transactions, pageable, transactions.size()));
        Page<Transaction> output =
                transactionService.getTransactionsByOrder(pageable, transactionSpecification, orderId);
        assertEquals(transactions.size(), output.getNumberOfElements());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldThrowEntityNotFoundWhenPagingAndFilteringTransactionsByOrder() {
        User user1 = getUsersList().get(0);
        Stock stock = getStocksList().get(0);
        AllOrders order =
                createBuyingAllOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        Long orderId = order.getId();

        when(allOrdersRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> transactionService.getTransactionsByOrder(pageable, transactionSpecification, orderId));
    }

    @Test
    void shouldReturnTransactionByStockIdForPricing() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        ArchivedOrder order1 =
                createSellingArchivedOrder(1L, stock.getAmount(),  BigDecimal.ONE, OffsetDateTime.now(), user, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, stock.getAmount(),  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = new ArrayList<>(Arrays.asList(
            new Transaction(1L, OffsetDateTime.now().minusDays(1), order1.getAmount(), order1.getPrice(), order1, order2),
            new Transaction(2L, OffsetDateTime.now(), order1.getAmount(), order1.getPrice(), order1, order2)
        ));
        List<Transaction> expected = Collections.singletonList(transactions.get(0));

        when(transactionService.getTransactionsByStockIdForPricing(stock.getId(), stock.getAmount()))
                .thenReturn(transactions);
        List<Transaction> output = transactionService.getTransactionsByStockIdForPricing(stock.getId(), stock.getAmount());
        assertEquals(expected.size(), output.size());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(expected.get(i), output.get(i));
        }
    }

    @Test
    void shouldReturnTransactionByStockIdForPricingWithoutFiltering() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        ArchivedOrder order1 =
                createSellingArchivedOrder(1L, stock.getAmount(),  BigDecimal.ONE, OffsetDateTime.now(), user, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, stock.getAmount(),  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = new ArrayList<>(Arrays.asList(
            new Transaction(1L, OffsetDateTime.now().minusDays(1), order1.getAmount()/2, order1.getPrice(), order1, order2),
            new Transaction(2L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2)
        ));

        when(transactionService.getTransactionsByStockIdForPricing(stock.getId(), stock.getAmount()))
                .thenReturn(transactions);
        List<Transaction> output = transactionService.getTransactionsByStockIdForPricing(stock.getId(), stock.getAmount());
        assertEquals(transactions.size(), output.size());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.get(i));
        }
    }

    @Test
    void shouldPageAndFilterUsersTransactionsAllTransactionTypes() {
        User user1 = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        ArchivedOrder order1 =
                createBuyingArchivedOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2),
                new Transaction(2L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        Long userId = user1.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(transactions, pageable, transactions.size()));
        Page<Transaction> output =
            transactionService.getUserTransactions(pageable, transactionSpecification, userId, true, true);
        assertEquals(transactions.size(), output.getNumberOfElements());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldPageAndFilterUsersTransactionsOnlyBuyingOrders() {
        User user1 = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        ArchivedOrder order1 =
                createBuyingArchivedOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2),
                new Transaction(2L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        Long userId = user1.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(transactions, pageable, transactions.size()));
        Page<Transaction> output =
            transactionService.getUserTransactions(pageable, transactionSpecification, userId, false, true);
        assertEquals(transactions.size(), output.getNumberOfElements());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldPageAndFilterUsersTransactionsOnlySellingOrders() {
        User user1 = getUsersList().get(0);
        User user2 = getUsersList().get(2);
        Stock stock = getStocksList().get(0);
        ArchivedOrder order1 =
                createBuyingArchivedOrder(1L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user1, stock);
        ArchivedOrder order2 =
                createSellingArchivedOrder(2L, 100,  BigDecimal.ONE, OffsetDateTime.now(), user2, stock);
        List<Transaction> transactions = Arrays.asList(
                new Transaction(1L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2),
                new Transaction(2L, OffsetDateTime.now(), order1.getAmount()/2, order1.getPrice(), order1, order2)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        Long userId = user2.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user2));
        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(transactions, pageable, transactions.size()));
        Page<Transaction> output =
                transactionService.getUserTransactions(pageable, transactionSpecification, userId, true, false);
        assertEquals(transactions.size(), output.getNumberOfElements());
        for (int i = 0; i < transactions.size(); i++) {
            assertEquals(transactions.get(i), output.getContent().get(i));
        }
    }

    @Test
    void shouldPageAndFilterUsersTransactionsNoneTransactions() {
        User user2 = getUsersList().get(2);
        Pageable pageable = PageRequest.of(0, 20);
        Specification<Transaction> transactionSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 50);
        Long userId = user2.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user2));
        Page<Transaction> output =
                transactionService.getUserTransactions(pageable, transactionSpecification, userId, false, false);
        assertEquals(0, output.getNumberOfElements());
    }

    public static void assertTransaction(Transaction output, Transaction expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertEquals(expected.getDate(), output.getDate()),
                () -> assertEquals(expected.getUnitPrice(), output.getUnitPrice()),
                () -> assertArchivedOrder(expected.getBuyingOrder(), output.getBuyingOrder()),
                () -> assertArchivedOrder(expected.getSellingOrder(), output.getSellingOrder()));
    }

}
