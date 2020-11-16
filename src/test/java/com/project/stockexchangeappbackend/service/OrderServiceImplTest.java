package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.dto.StockDTO;
import com.project.stockexchangeappbackend.entity.*;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

import java.util.List;
import java.util.Optional;

import static com.project.stockexchangeappbackend.service.ResourceServiceImplTest.createCustomResource;
import static com.project.stockexchangeappbackend.service.StockServiceImplTest.*;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.assertUser;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.createCustomUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    OrderServiceImpl orderService;

    @Mock
    ModelMapper modelMapper;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ArchivedOrderRepository archivedOrderRepository;

    @Mock
    AllOrdersRepository allOrdersRepository;

    @Mock
    StockRepository stockRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ResourceRepository resourceRepository;


    @Test
    void shouldPageAndFilterOrders() {
        Stock stock = createCustomStock(1L, "WIG30", "W30", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        Order order1 = createCustomOrder(1L, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                BigDecimal.ONE, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2), null, user, stock);
        Order order2 = createCustomOrder(2L, 250, 250, OrderType.SELLING_ORDER, PriceType.EQUAL,
                BigDecimal.ONE, OffsetDateTime.now().minusHours(2), OffsetDateTime.now().plusHours(2), null, user, stock);
        List<AllOrders> orders = Arrays.asList(
                createCustomAllOrdersInstance(order1),
                createCustomAllOrdersInstance(order2)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<AllOrders> allOrdersSpecification =
                (Specification<AllOrders>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), 1);
        when(allOrdersRepository.findAll(allOrdersSpecification, pageable))
                .thenReturn(new PageImpl<>(orders, pageable, orders.size()));
        Page<AllOrders> output = orderService.findAllOrders(pageable, allOrdersSpecification);
        assertEquals(orders.size(), output.getNumberOfElements());
        for (int i = 0; i < orders.size(); i++) {
            assertEquals(output.getContent().get(i), orders.get(i));
        }
    }

    @Test
    void shouldReturnOrder() {
        Long id = 1L;
        Stock stock = createCustomStock(1L, "WIG30", "W30", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        Order order = createCustomOrder(id, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                BigDecimal.ONE, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2), null, user, stock);
        AllOrders allOrder = createCustomAllOrdersInstance(order);

        when(allOrdersRepository.findById(id)).thenReturn(Optional.of(allOrder));
        assertAllOrder(orderService.findOrderById(id), allOrder);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingOrderById() {
        Long id = 1L;
        when(allOrdersRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.findOrderById(id));
    }

    @Test
    void shouldCreateNewOrder(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        StockDTO stockDTO = createCustomStockDTO(1L, null, null, null, null);
        OrderDTO orderDTO = createCustomOrderDTO(100, OffsetDateTime.now().plusHours(1), OrderType.SELLING_ORDER,
                PriceType.EQUAL, BigDecimal.ONE, stockDTO);
        Stock stock = createCustomStock(1L, "WIG20", "W20", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        Order order = createCustomOrder(null, orderDTO.getAmount(), null, orderDTO.getOrderType(),
                orderDTO.getPriceType(), orderDTO.getPrice(), null, orderDTO.getDateExpiration(),
                null, null, null);
        Resource resource = createCustomResource(1L, stock, user, stock.getAmount());
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull
            (Mockito.eq(stock), Mockito.eq(user), Mockito.eq(OrderType.SELLING_ORDER), Mockito.any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(modelMapper.map(orderDTO, Order.class)).thenReturn(order);
        assertAll(() -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndUserNotHavingStock(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        StockDTO stockDTO = createCustomStockDTO(1L, null, null, null, null);
        OrderDTO orderDTO = createCustomOrderDTO(100, OffsetDateTime.now().plusHours(1), OrderType.SELLING_ORDER,
                PriceType.EQUAL, BigDecimal.ONE, stockDTO);
        Stock stock = createCustomStock(1L, "WIG20", "W20", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        Resource resource = createCustomResource(1L, stock, user, orderDTO.getAmount() - 1);
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull
                (Mockito.eq(stock), Mockito.eq(user), Mockito.eq(OrderType.SELLING_ORDER), Mockito.any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndUserNotHavingAvailableStock(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        StockDTO stockDTO = createCustomStockDTO(1L, null, null, null, null);
        OrderDTO orderDTO = createCustomOrderDTO(100, OffsetDateTime.now().plusHours(1), OrderType.SELLING_ORDER,
                PriceType.EQUAL, BigDecimal.ONE, stockDTO);
        Stock stock = createCustomStock(1L, "WIG20", "W20", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        Order order = createCustomOrder(1L, 100, 100, OrderType.SELLING_ORDER, PriceType.EQUAL,
                BigDecimal.ONE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusHours(1), null, user, stock);
        Resource resource = createCustomResource(1L, stock, user, orderDTO.getAmount());
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull
                (Mockito.eq(stock), Mockito.eq(user), Mockito.eq(OrderType.SELLING_ORDER), Mockito.any(OffsetDateTime.class)))
                .thenReturn(Collections.singletonList(order));
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndSellingOrderIncompatible(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        StockDTO stockDTO = createCustomStockDTO(1L, null, null, null, null);
        OrderDTO orderDTO = createCustomOrderDTO(100, OffsetDateTime.now().plusHours(1), OrderType.SELLING_ORDER,
                PriceType.LESS_OR_EQUAL, BigDecimal.ONE, stockDTO);
        Stock stock = createCustomStock(1L, "WIG20", "W20", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        Resource resource = createCustomResource(1L, stock, user, orderDTO.getAmount());
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull
                (Mockito.eq(stock), Mockito.eq(user), Mockito.eq(OrderType.SELLING_ORDER), Mockito.any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndNotEnoughStock(@Mock SecurityContext securityContext,
                                                                                   @Mock Authentication authentication) {
        StockDTO stockDTO = createCustomStockDTO(1L, null, null, null, null);
        OrderDTO orderDTO = createCustomOrderDTO(100, OffsetDateTime.now().plusHours(1), OrderType.BUYING_ORDER,
                PriceType.EQUAL, BigDecimal.ONE, stockDTO);
        Stock stock = createCustomStock(1L, "WIG20", "W20", orderDTO.getAmount() - 1, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndBuyingOrderIncompatible(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        StockDTO stockDTO = createCustomStockDTO(1L, null, null, null, null);
        OrderDTO orderDTO = createCustomOrderDTO(100, OffsetDateTime.now().plusHours(1), OrderType.BUYING_ORDER,
                PriceType.GREATER_OR_EQUAL, BigDecimal.ONE, stockDTO);
        Stock stock = createCustomStock(1L, "WIG20", "W20", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndUserNotFound(@Mock SecurityContext securityContext,
                                                                                 @Mock Authentication authentication) {
        StockDTO stockDTO = createCustomStockDTO(1L, null, null, null, null);
        OrderDTO orderDTO = createCustomOrderDTO(100, OffsetDateTime.now().plusHours(1), OrderType.BUYING_ORDER,
                PriceType.GREATER_OR_EQUAL, BigDecimal.ONE, stockDTO);
        Stock stock = createCustomStock(1L, "WIG20", "W20", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndStockNotFound(@Mock SecurityContext securityContext,
                                                                                  @Mock Authentication authentication) {
        StockDTO stockDTO = createCustomStockDTO(1L, null, null, null, null);
        OrderDTO orderDTO = createCustomOrderDTO(100, OffsetDateTime.now().plusHours(1), OrderType.BUYING_ORDER,
                PriceType.GREATER_OR_EQUAL, BigDecimal.ONE, stockDTO);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }


    @Test
    void shouldListActiveBuyingOrders() {
        Long id = 1L;
        Stock stock = createCustomStock(1L, "WIG30", "W30", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        List<Order> orders = Arrays.asList(
                createCustomOrder(id, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                        BigDecimal.ONE, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2), null, user, stock),
                createCustomOrder(id, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                        BigDecimal.ONE, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2), null, user, stock));
        when(orderRepository.findByOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                Mockito.eq(OrderType.BUYING_ORDER), Mockito.any(OffsetDateTime.class)))
                .thenReturn(orders);
        List<Order> output = orderService.getActiveBuyingOrders();
        assertEquals(orders.size(), output.size());
        for (int i=0; i<output.size(); i++) {
            assertOrder(output.get(i), orders.get(i));
        }
    }

    @Test
    void shouldListActiveSellingOrdersByStock() {
        Long id = 1L;
        Stock stock = createCustomStock(1L, "WIG30", "W30", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        List<Order> orders = Arrays.asList(
                createCustomOrder(id, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                        BigDecimal.ONE, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2), null, user, stock),
                createCustomOrder(id, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                        BigDecimal.ONE, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2), null, user, stock));
        when(orderRepository.findByStockAndOrderTypeAndPriceIsLessThanEqualAndDateExpirationIsAfterAndDateClosingIsNullOrderByPrice(
                Mockito.eq(stock), Mockito.eq(OrderType.SELLING_ORDER), Mockito.eq(BigDecimal.ONE), Mockito.any(OffsetDateTime.class)))
                .thenReturn(orders);
        List<Order> output = orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(stock, BigDecimal.ONE);
        assertEquals(orders.size(), output.size());
        for (int i=0; i<output.size(); i++) {
            assertOrder(output.get(i), orders.get(i));
        }
    }

    @Test
    void shouldMoveInactiveOrders() {
        Stock stock = createCustomStock(1L, "WIG30", "W30", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        List<Order> orders = Arrays.asList(
                createCustomOrder(1L, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                        BigDecimal.ONE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().minusHours(2), null, user, stock),
                createCustomOrder(2L, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                        BigDecimal.ONE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().minusHours(2), null, user, stock));
        when(orderRepository.findByDateExpirationIsBeforeOrRemainingAmountOrDateClosingIsNotNull(
                Mockito.any(OffsetDateTime.class), Mockito.eq(0))).thenReturn(orders);
        assertAll(() -> orderService.moveInactiveOrders());
    }

    @Test
    void shouldDeactivateOrder(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Long id = 1L;
        Stock stock = createCustomStock(1L, "WIG20", "W20", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO, Role.USER);
        Order order = createCustomOrder(id, 100, 90, OrderType.BUYING_ORDER, PriceType.EQUAL,
                BigDecimal.TEN, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusHours(2),
                null, user, stock);
        ArchivedOrder archivedOrder = createCustomArchivedOrder(order);
        SecurityContextHolder.setContext(securityContext);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(archivedOrderRepository.findById(id)).thenReturn(Optional.of(archivedOrder));
        assertAll(() -> orderService.deactivateOrder(id));
    }

    @Test
    void shouldDeactivateOrderAndNotArchived(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Long id = 1L;
        Stock stock = createCustomStock(1L, "WIG20", "W20", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO);
        Order order = createCustomOrder(id, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                BigDecimal.TEN, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusHours(2),
                null, user, stock);
        ArchivedOrder archivedOrder = createCustomArchivedOrder(order);
        SecurityContextHolder.setContext(securityContext);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(archivedOrderRepository.findById(id)).thenReturn(Optional.empty());
        when(modelMapper.map(order, ArchivedOrder.class)).thenReturn(archivedOrder);
        assertAll(() -> orderService.deactivateOrder(id));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenDeactivatingOrder(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Long id = 1L;
        Stock stock = createCustomStock(1L, "WIG20", "W20", 1024, BigDecimal.TEN);
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ZERO, Role.USER);
        User user2 = createCustomUser(2L, "test2@test.pl", "Timmy", "Lawok", BigDecimal.ZERO, Role.USER);
        Order order = createCustomOrder(id, 100, 100, OrderType.BUYING_ORDER, PriceType.EQUAL,
                BigDecimal.TEN, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusHours(2),
                null, user, stock);
        SecurityContextHolder.setContext(securityContext);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmailIgnoreCase(user2.getEmail())).thenReturn(Optional.of(user2));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("test2@test.pl");
        assertThrows(AccessDeniedException.class, () -> orderService.deactivateOrder(id));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeactivatingOrder() {
        Long id = 1L;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.deactivateOrder(id));
    }

    public static void assertOrder(Order output, Order expected) {

        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertEquals(expected.getRemainingAmount(), output.getRemainingAmount()),
                () -> assertEquals(expected.getDateCreation(), output.getDateCreation()),
                () -> assertEquals(expected.getDateExpiration(), output.getDateExpiration()),
                () -> assertEquals(expected.getDateClosing(), output.getDateClosing()),
                () -> assertEquals(expected.getOrderType(), output.getOrderType()),
                () -> assertEquals(expected.getPriceType(), output.getPriceType()),
                () -> assertEquals(expected.getPrice(), output.getPrice()),
                () -> assertStock(expected.getStock(), output.getStock()),
                () -> assertUser(expected.getUser(), output.getUser()));
    }

    public static void assertAllOrder(AllOrders output, AllOrders expected) {

        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertEquals(expected.getRemainingAmount(), output.getRemainingAmount()),
                () -> assertEquals(expected.getDateCreation(), output.getDateCreation()),
                () -> assertEquals(expected.getDateExpiration(), output.getDateExpiration()),
                () -> assertEquals(expected.getDateClosing(), output.getDateClosing()),
                () -> assertEquals(expected.getOrderType(), output.getOrderType()),
                () -> assertEquals(expected.getPriceType(), output.getPriceType()),
                () -> assertEquals(expected.getPrice(), output.getPrice()),
                () -> assertStock(expected.getStock(), output.getStock()),
                () -> assertUser(expected.getUser(), output.getUser()));
    }

    public static Order createCustomOrder(Long id, Integer amount, Integer remainingAmount, OrderType orderType,
                                          PriceType priceType, BigDecimal price, OffsetDateTime dateCreation,
                                          OffsetDateTime dateExpiration, OffsetDateTime dateClosing, User user,
                                          Stock stock) {
        return Order.builder()
                .id(id).amount(amount).remainingAmount(remainingAmount)
                .dateCreation(dateCreation).dateClosing(dateClosing).dateExpiration(dateExpiration)
                .orderType(orderType).priceType(priceType).price(price)
                .stock(stock).user(user)
                .build();
    }

    public static ArchivedOrder createCustomArchivedOrder(Order order){
        return ArchivedOrder.builder()
                .id(order.getId())
                .amount(order.getAmount())
                .remainingAmount(order.getRemainingAmount())
                .dateClosing(order.getDateClosing())
                .dateExpiration(order.getDateExpiration())
                .dateCreation(order.getDateCreation())
                .orderType(order.getOrderType())
                .priceType(order.getPriceType())
                .price(order.getPrice())
                .stock(order.getStock())
                .user(order.getUser())
                .build();
    }

    public static OrderDTO createCustomOrderDTO(Integer amount, OffsetDateTime dateExpiration, OrderType orderType,
                                                PriceType priceType, BigDecimal price, StockDTO stockDTO) {
        return OrderDTO.builder()
                .amount(amount).dateExpiration(dateExpiration)
                .orderType(orderType).priceType(priceType).price(price)
                .stock(stockDTO)
                .build();
    }

    public static AllOrders createCustomAllOrdersInstance(Order order) {
        return AllOrders.builder()
                .id(order.getId())
                .amount(order.getAmount())
                .remainingAmount(order.getRemainingAmount())
                .dateClosing(order.getDateClosing())
                .dateExpiration(order.getDateExpiration())
                .dateCreation(order.getDateCreation())
                .orderType(order.getOrderType())
                .priceType(order.getPriceType())
                .price(order.getPrice())
                .stock(order.getStock())
                .user(order.getUser())
                .build();
    }

}