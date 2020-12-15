package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.CreateOrderDTO;
import com.project.stockexchangeappbackend.dto.StockDTO;
import com.project.stockexchangeappbackend.entity.*;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.*;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

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
public class OrderServiceImplTest {

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
    @DisplayName("Getting order by id as admin")
    void shouldReturnOrderAsAdmin(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Long id = 1L;
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        AllOrders allOrder =
                createBuyingAllOrder(id, 10, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock);
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenReturn(authorities);
        when(allOrdersRepository.findById(id)).thenReturn(Optional.of(allOrder));
        assertAllOrder(orderService.findOrderById(id), allOrder);
    }

    @Test
    @DisplayName("Getting order by id as user")
    void shouldReturnOrderAsUser(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Long id = 1L;
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        AllOrders allOrder =
                createBuyingAllOrder(id, 10, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock);
        allOrder.setUser(null);
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenReturn(authorities);
        when(allOrdersRepository.findById(id)).thenReturn(Optional.of(allOrder));
        assertAllOrder(orderService.findOrderById(id), allOrder);
    }

    @Test
    @DisplayName("Getting order by id when order not found")
    void shouldThrowEntityNotFoundExceptionWhenGettingOrderById() {
        Long id = 1L;
        when(allOrdersRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.findOrderById(id));
    }

    @Test
    @DisplayName("Creating new order")
    void shouldCreateNewOrder(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateOrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
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
        when(orderRepository.save(order)).thenReturn(order);
        assertAll(() -> orderService.createOrder(orderDTO));
    }

    @Test
    @DisplayName("Creating new order when user not having enough stock")
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndUserNotHavingStock(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateOrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
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
    @DisplayName("Creating new order when user not having enough available stock")
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndUserNotHavingAvailableStock(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateOrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
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
    @DisplayName("Creating new selling order when order is incompatible")
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndSellingOrderIncompatible(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateOrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
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
    @DisplayName("Creating new buying order when desired amount of stock grater than registered amount of stock")
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndNotEnoughStock(@Mock SecurityContext securityContext,
                                                                                   @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateOrderDTO orderDTO = createBuyingOrderDTO(stock.getAmount()*2, OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    @DisplayName("Creating new buying order when order is incompatible")
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndBuyingOrderIncompatible(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateOrderDTO orderDTO = createBuyingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
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
    @DisplayName("Creating new order when using different tags")
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndStockAndUserTaggedOthersTags(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        user.setTag(getTagsList().get(1));
        CreateOrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
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
    @DisplayName("Creating new order when user not found")
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndUserNotFound(@Mock SecurityContext securityContext,
                                                                                 @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        String username = "none";
        CreateOrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());
        SecurityContextHolder.setContext(securityContext);

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.of(stock));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(username);
        when(userRepository.findByEmailIgnoreCase(username)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    @DisplayName("Creating new order when stock not found")
    void shouldThrowInvalidInputDataExceptionWhenCreatingNewOrderAndStockNotFound() {
        Stock stock = getStocksList().get(0);
        CreateOrderDTO orderDTO = createSellingOrderDTO(stock.getAmount(), OffsetDateTime.now().plusHours(1),
                BigDecimal.ONE, stock.getId());

        when(stockRepository.findByIdAndIsDeletedFalse(orderDTO.getStock().getId())).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> orderService.createOrder(orderDTO));
    }

    @Test
    @DisplayName("Paging and filtering orders as admin")
    void shouldPageAndFilterOrdersAsAdmin(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        List<AllOrders> orders = Arrays.asList(
                createBuyingAllOrder(1L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock),
                createSellingAllOrder(2L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<AllOrders> allOrdersSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 100);
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenReturn(authorities);
        when(allOrdersRepository.findAll(allOrdersSpecification, pageable))
                .thenReturn(new PageImpl<>(orders, pageable, orders.size()));
        Page<AllOrders> output = orderService.findAllOrders(pageable, allOrdersSpecification);
        assertEquals(orders.size(), output.getNumberOfElements());
        for (int i = 0; i < orders.size(); i++) {
            assertAllOrder(output.getContent().get(i), orders.get(i));
        }
    }

    @Test
    @DisplayName("Paging and filtering orders as user")
    void shouldPageAndFilterOrdersAsUser(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        List<AllOrders> orders = Arrays.asList(
                createBuyingAllOrder(1L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock),
                createSellingAllOrder(2L, 100, BigDecimal.ONE, OffsetDateTime.now().plusHours(2), user, stock)
        );
        orders.forEach(order -> order.setUser(null));
        Pageable pageable = PageRequest.of(0, 20);
        Specification<AllOrders> allOrdersSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("amount"), 100);
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getAuthorities())
                .thenReturn(authorities);
        when(allOrdersRepository.findAll(allOrdersSpecification, pageable))
                .thenReturn(new PageImpl<>(orders, pageable, orders.size()));
        Page<AllOrders> output = orderService.findAllOrders(pageable, allOrdersSpecification);
        assertEquals(orders.size(), output.getNumberOfElements());
        for (int i = 0; i < orders.size(); i++) {
            assertAllOrder(output.getContent().get(i), orders.get(i));
        }
    }

    @Test
    @DisplayName("Deactivation of order")
    void shouldDeactivateOrder(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        Order order = createSellingOrder(1L, stock.getAmount(), BigDecimal.ONE,
                OffsetDateTime.now().plusHours(1), user, stock);
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
    @DisplayName("Deactivation of order when order not already archived")
    void shouldDeactivateOrderAndNotArchived(@Mock SecurityContext securityContext,
                                             @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        Order order = createSellingOrder(1L, stock.getAmount(), BigDecimal.ONE,
                OffsetDateTime.now().plusHours(1), user, stock);
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
    @DisplayName("Deactivation of not owned order")
    void shouldThrowAccessDeniedExceptionWhenDeactivatingOrder(
            @Mock SecurityContext securityContext, @Mock Authentication authentication) {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        User user2 = getUsersList().get(1);
        user2.setRole(Role.USER);
        Order order = createSellingOrder(1L, stock.getAmount(), BigDecimal.ONE,
                OffsetDateTime.now().plusHours(1), user, stock);
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
    @DisplayName("Deactivation of non-active order")
    void shouldThrowEntityNotFoundExceptionWhenDeactivatingOrder() {
        Long id = 1L;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.deactivateOrder(id));
    }

    @Test
    @DisplayName("Paging and filtering user's orders")
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
    @DisplayName("Paging and filtering user's orders when user not found")
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
    @DisplayName("Listing active buying orders")
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
    @DisplayName("Listing active selling orders")
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
    @DisplayName("Archiving expired orders")
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
    @DisplayName("Paging and filtering logged in user's orders")
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

    @Test
    @DisplayName("Refreshing state of active order by id")
    void shouldRefreshStateOfOrder() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        Order order = createSellingOrder(1L, 100, BigDecimal.ONE, OffsetDateTime.now(), user, stock);
        Long id = order.getId();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        assertOrder(orderService.refreshObjectById(id).get(), order);
    }

    @Test
    @DisplayName("Refreshing state of active order by id when order not found")
    void shouldRefreshStateOfOrderWhenOrderNotFound() {
        Long id = 1L;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        assertTrue(orderService.refreshObjectById(id).isEmpty());
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
                () -> assertEquals(expected.getUser(), output.getUser()));
        if (expected.getUser() != null) {
            assertUser(expected.getUser(), output.getUser());
        }
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
                () -> assertEquals(expected.getUser(), output.getUser()));
        if (expected.getUser() != null) {
            assertUser(expected.getUser(), output.getUser());
        }
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

    public static CreateOrderDTO createSellingOrderDTO(Integer amount, OffsetDateTime dateExpiration,
                                                       BigDecimal price, Long stockId) {
        return CreateOrderDTO.builder()
                .amount(amount).dateExpiration(dateExpiration)
                .orderType(OrderType.SELLING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(StockDTO.builder().id(stockId).build())
                .build();
    }

    public static CreateOrderDTO createBuyingOrderDTO(Integer amount, OffsetDateTime dateExpiration,
                                                       BigDecimal price, Long stockId) {
        return CreateOrderDTO.builder()
                .amount(amount).dateExpiration(dateExpiration)
                .orderType(OrderType.BUYING_ORDER).priceType(PriceType.EQUAL).price(price)
                .stock(StockDTO.builder().id(stockId).build())
                .build();
    }

}
