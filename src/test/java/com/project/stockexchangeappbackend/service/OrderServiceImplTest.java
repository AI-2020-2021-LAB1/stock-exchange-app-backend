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

import static com.project.stockexchangeappbackend.service.StockServiceImplTest.assertStock;
import static com.project.stockexchangeappbackend.service.StockServiceImplTest.getStocksList;
import static com.project.stockexchangeappbackend.service.TagServiceImplTest.getTagsList;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.assertUser;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.getUsersList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void shouldReturnOrder() {
        Long id = 1L;
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        AllOrders allOrder =
                createBuyingAllOrder(id, 10, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock);

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
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        Order order = createSellingOrder(1L, orderDTO.getAmount(), orderDTO.getPrice(),
                orderDTO.getDateExpiration(), user, stock);
        Resource resource = Resource.builder().id(1L).stock(stock).user(user).amount(stock.getAmount()).build();
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(user), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(modelMapper.map(orderDTO, Order.class)).thenReturn(order);
        assertAll(() -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndUserNotHavingStock(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        Resource resource = Resource.builder().id(1L).stock(stock).user(user).amount(stock.getAmount() - 1).build();
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(user), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndUserNotHavingAvailableStock(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        SecurityContextHolder.setContext(securityContext);
        Order order = createSellingOrder(1L, orderDTO.getAmount(), orderDTO.getPrice(),
                orderDTO.getDateExpiration(), user, stock);
        Resource resource = Resource.builder().id(1L).stock(stock).user(user).amount(order.getAmount()/2).build();
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(user), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.singletonList(order));
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndSellingOrderIncompatible(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        orderDTO.setPriceType(PriceType.LESS_OR_EQUAL);
        Resource resource = Resource.builder().id(1L).stock(stock).user(user).amount(stock.getAmount()).build();
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(user), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndNotEnoughStock(@Mock SecurityContext securityContext,
                                                                                   @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        OrderDTO orderDTO = createBuyingOrderDTO(stock.getAmount()*2, OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
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
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        OrderDTO orderDTO = createBuyingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        orderDTO.setPriceType(PriceType.GREATER_OR_EQUAL);
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndStockAndUserTaggedOthersTags(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        user.setTag(getTagsList().get(1));
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        Resource resource = Resource.builder().id(1L).stock(stock).user(user).amount(stock.getAmount()).build();
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
        user.setTag(getTagsList().get(0));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndUserNotFound(@Mock SecurityContext securityContext,
                                                                                 @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        String username = "none";
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(username);
        when(userRepository.findByEmailIgnoreCase(username)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndStockNotFound() {
        Stock stock = getStocksList().get(0);
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    void shouldPageAndFilterOrders() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        List<AllOrders> orders = Arrays.asList(
                createBuyingAllOrder(1L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock),
                createSellingAllOrder(2L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<AllOrders> allOrdersSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 100);

        when(allOrdersRepository.findAll(allOrdersSpecification, pageable))
                .thenReturn(new PageImpl<>(orders, pageable, orders.size()));
        Page<AllOrders> output = orderService.findAllOrders(pageable, allOrdersSpecification);
        assertEquals(orders.size(), output.getNumberOfElements());
        for (int i = 0; i < orders.size(); i++) {
            assertAllOrder(output.getContent().get(i), orders.get(i));
        }
    }

    @Test
    void shouldDeactivateOrder(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        Order order = createSellingOrder(1L, orderDTO.getAmount(), orderDTO.getPrice(),
                orderDTO.getDateExpiration(), user, stock);
        ArchivedOrder archivedOrder = convertOrder(order);
        Long id = stock.getId();
        SecurityContextHolder.setContext(securityContext);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(archivedOrderRepository.findById(id)).thenReturn(Optional.of(archivedOrder));
        assertAll(() -> orderService.deactivateOrder(id));
    }

    @Test
    void shouldDeactivateOrderAndNotArchived(@Mock SecurityContext securityContext,
                                             @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        Order order = createSellingOrder(1L, orderDTO.getAmount(), orderDTO.getPrice(),
                orderDTO.getDateExpiration(), user, stock);
        ArchivedOrder archivedOrder = convertOrder(order);
        Long id = stock.getId();
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
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        User user2 = getUsersList().get(1);
        user2.setRole(Role.USER);
        OrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        Order order = createSellingOrder(1L, orderDTO.getAmount(), orderDTO.getPrice(),
                orderDTO.getDateExpiration(), user, stock);
        Long id = stock.getId();
        SecurityContextHolder.setContext(securityContext);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(userRepository.findByEmailIgnoreCase(user2.getEmail())).thenReturn(Optional.of(user2));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user2.getEmail());
        assertThrows(AccessDeniedException.class, () -> orderService.deactivateOrder(id));
        user2.setRole(Role.ADMIN);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeactivatingOrder() {
        Long id = 1L;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.deactivateOrder(id));
    }

    @Test
    void shouldPageAndFilterOrdersByUser() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        List<AllOrders> orders = Arrays.asList(
                createBuyingAllOrder(1L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock),
                createSellingAllOrder(2L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<AllOrders> allOrdersSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 100);
        Long userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(allOrdersRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(orders, pageable, orders.size()));
        Page<AllOrders> output = orderService.getOrdersByUser(pageable, allOrdersSpecification, userId);
        assertEquals(orders.size(), output.getNumberOfElements());
        for (int i = 0; i < orders.size(); i++) {
            assertAllOrder(output.getContent().get(i), orders.get(i));
        }
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPagingAndFilteringOrdersByUser() {
        Pageable pageable = PageRequest.of(0, 20);
        Specification<AllOrders> allOrdersSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 100);
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> orderService.getOrdersByUser(pageable, allOrdersSpecification, userId));
    }

    @Test
    void shouldListActiveBuyingOrders() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        List<Order> orders = Collections.singletonList(
                createBuyingOrder(1L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock)
        );

        when(orderRepository.findByOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(OrderType.BUYING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(orders);
        List<Order> output = orderService.getActiveBuyingOrders();
        assertEquals(orders.size(), output.size());
        for (int i=0; i<output.size(); i++) {
            assertOrder(output.get(i), orders.get(i));
        }
    }

    @Test
    void shouldListActiveSellingOrdersByStock() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        List<Order> orders = Collections.singletonList(
                createSellingOrder(1L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock)
        );

        when(orderRepository.findByStockAndOrderTypeAndPriceIsLessThanEqualAndDateExpirationIsAfterAndDateClosingIsNullOrderByPrice(
                eq(stock), eq(OrderType.SELLING_ORDER), eq(BigDecimal.TEN), any(OffsetDateTime.class)))
                .thenReturn(orders);
        List<Order> output = orderService.getActiveSellingOrdersByStockAndPriceLessThanEqual(stock, BigDecimal.TEN);
        assertEquals(orders.size(), output.size());
        for (int i=0; i<output.size(); i++) {
            assertOrder(output.get(i), orders.get(i));
        }
    }

    @Test
    void shouldMoveInactiveOrders() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        List<Order> orders = Collections.singletonList(
                createSellingOrder(1L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock)
        );
        orders.get(0).setRemainingAmount(0);

        when(orderRepository.findByDateExpirationIsBeforeOrRemainingAmountOrDateClosingIsNotNull(
                any(OffsetDateTime.class), eq(0)))
                .thenReturn(orders);
        assertAll(() -> orderService.moveInactiveOrders());
    }

    @Test
    void shouldPageAndFilterOwnedOrders(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        List<AllOrders> orders = Arrays.asList(
                createBuyingAllOrder(1L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock),
                createSellingAllOrder(2L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<AllOrders> allOrdersSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 100);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(allOrdersRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(orders, pageable, orders.size()));
        Page<AllOrders> output = orderService.getOwnedOrders(pageable, allOrdersSpecification);
        assertEquals(orders.size(), output.getNumberOfElements());
        for (int i = 0; i < orders.size(); i++) {
            assertAllOrder(output.getContent().get(i), orders.get(i));
        }
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

    public static void assertArchivedOrder(ArchivedOrder output, ArchivedOrder expected) {
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

    public static Order createSellingOrder(Long id, Integer amount, BigDecimal price,
                                                 OffsetDateTime dateExpiration, User user, Stock stock) {
        return Order.builder()
                .id(id).amount(amount).remainingAmount(amount)
                .dateCreation(OffsetDateTime.now()).dateExpiration(dateExpiration)
                .orderType(OrderType.SELLING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(stock).user(user)
                .build();
    }

    public static Order createBuyingOrder(Long id, Integer amount, BigDecimal price,
                                                OffsetDateTime dateExpiration, User user, Stock stock) {
        return Order.builder()
                .id(id).amount(amount).remainingAmount(amount)
                .dateCreation(OffsetDateTime.now()).dateExpiration(dateExpiration)
                .orderType(OrderType.BUYING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(stock).user(user)
                .build();
    }

    public static AllOrders createSellingAllOrder(Long id, Integer amount, BigDecimal price,
                                                 OffsetDateTime dateExpiration, User user, Stock stock) {
        return AllOrders.builder()
                .id(id).amount(amount).remainingAmount(amount)
                .dateCreation(OffsetDateTime.now()).dateExpiration(dateExpiration)
                .orderType(OrderType.SELLING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(stock).user(user)
                .build();
    }

    public static AllOrders createBuyingAllOrder(Long id, Integer amount, BigDecimal price,
                                                OffsetDateTime dateExpiration, User user, Stock stock) {
        return AllOrders.builder()
                .id(id).amount(amount).remainingAmount(amount)
                .dateCreation(OffsetDateTime.now()).dateExpiration(dateExpiration)
                .orderType(OrderType.BUYING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(stock).user(user)
                .build();
    }

    public static ArchivedOrder createSellingArchivedOrder(Long id, Integer amount, BigDecimal price,
                                                           OffsetDateTime dateExpiration, User user, Stock stock) {
        return ArchivedOrder.builder()
                .id(id).amount(amount).remainingAmount(0)
                .dateCreation(OffsetDateTime.now()).dateExpiration(dateExpiration)
                .orderType(OrderType.SELLING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(stock).user(user)
                .build();
    }

    public static ArchivedOrder createBuyingArchivedOrder(Long id, Integer amount, BigDecimal price,
                                                          OffsetDateTime dateExpiration, User user, Stock stock) {
        return ArchivedOrder.builder()
                .id(id).amount(amount).remainingAmount(0)
                .dateCreation(OffsetDateTime.now()).dateExpiration(dateExpiration)
                .orderType(OrderType.BUYING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(stock).user(user)
                .build();
    }

    public static ArchivedOrder convertOrder(Order order){
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

    public static OrderDTO createSellingOrderDTO(Integer amount, OffsetDateTime dateExpiration,
                                                       BigDecimal price, Long stockId) {
        return OrderDTO.builder()
                .amount(amount).dateExpiration(dateExpiration)
                .orderType(OrderType.SELLING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(StockDTO.builder().id(stockId).build())
                .build();
    }

    public static OrderDTO createBuyingOrderDTO(Integer amount, OffsetDateTime dateExpiration,
                                                       BigDecimal price, Long stockId) {
        return OrderDTO.builder()
                .amount(amount).dateExpiration(dateExpiration)
                .orderType(OrderType.BUYING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(StockDTO.builder().id(stockId).build())
                .build();
    }

}
