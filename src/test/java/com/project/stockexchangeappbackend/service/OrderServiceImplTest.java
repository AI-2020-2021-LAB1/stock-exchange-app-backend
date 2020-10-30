package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.entity.ArchivedOrder;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.OrderType;
import com.project.stockexchangeappbackend.entity.PriceType;
import com.project.stockexchangeappbackend.repository.ArchivedOrderRepository;
import com.project.stockexchangeappbackend.repository.OrderRepository;
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
import static org.mockito.Mockito.*;


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

    @Test
    void shouldReturnOrderFromOrderEntity() {
        Long id = 1L;
        Order order = createCustomOrder(id);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        assertOrder(orderService.findOrderById(id), order);
    }

    @Test
    void shouldReturnOrderFromArchivedOrderEntity() {
        Long id = 1L;
        Order order = createCustomOrder(id);
        ArchivedOrder archivedOrder = createCustomArchivedOrder(order);
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        when(archivedOrderRepository.findById(id)).thenReturn(Optional.of(archivedOrder));
        when(modelMapper.map(archivedOrder, Order.class)).thenReturn(order);
        assertOrder(orderService.findOrderById(id), order);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingOrderById() {
        Long id = 1L;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        when(archivedOrderRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> orderService.findOrderById(id));
    }

    private static void assertOrder(Order output, Order expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertEquals(expected.getRemainingAmount(), output.getRemainingAmount()),
                () -> assertEquals(expected.getDateCreation(), output.getDateCreation()),
                () -> assertEquals(expected.getDateExpiration(), output.getDateExpiration()),
                () -> assertEquals(expected.getDateClosing(), output.getDateClosing()),
                () -> assertEquals(expected.getOrderType(), output.getOrderType()),
                () -> assertEquals(expected.getPriceType(), output.getPriceType()),
                () -> assertEquals(expected.getPrice(), output.getPrice()));
    }

    private static Order createCustomOrder (Long id) {
        return Order.builder()
                .id(id).amount(100).remainingAmount(100)
                .dateClosing(OffsetDateTime.now().minusHours(1))
                .dateExpiration(OffsetDateTime.now().plusHours(1))
                .orderType(OrderType.BUYING_ORDER)
                .priceType(PriceType.LESS_OR_EQUAL).price(BigDecimal.ONE)
                .build();
    }

    private static ArchivedOrder createCustomArchivedOrder (Order order) {
        return ArchivedOrder.builder()
                .id(order.getId()).amount(order.getAmount()).remainingAmount(order.getRemainingAmount())
                .dateClosing(order.getDateClosing()).dateExpiration(order.getDateExpiration())
                .dateCreation(order.getDateCreation())
                .orderType(order.getOrderType())
                .priceType(order.getPriceType()).price(order.getPrice())
                .build();
    }

}