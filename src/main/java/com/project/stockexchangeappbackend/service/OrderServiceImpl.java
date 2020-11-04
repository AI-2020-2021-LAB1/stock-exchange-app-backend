package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.entity.*;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.*;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ArchivedOrderRepository archivedOrderRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ModelMapper modelMapper;
    private final AllOrdersRepository allOrdersRepository;

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseGet(() -> modelMapper.map(archivedOrderRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Order Not Found")), Order.class));
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional
    public void createOrder(OrderDTO orderDTO) {
        Stock stock = stockRepository.findById(orderDTO.getStock().getId())
                .orElseThrow(() -> new InvalidInputDataException("Validation error",
                        Map.of("stock", "Stock company not found.")));
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new AccessDeniedException("Access Denied"));
        validateOrder(orderDTO, stock, user);
        Order order = modelMapper.map(orderDTO, Order.class);
        order.setStock(stock);
        order.setUser(user);
        order.setRemainingAmount(orderDTO.getAmount());
        order.setDateCreation(OffsetDateTime.now(ZoneId.systemDefault()));
        order.setDateClosing(null);
        orderRepository.save(order);
    }

    @Override
    @LogicBusinessMeasureTime
    public Page<AllOrders> findAllOrders(Pageable pageable, Specification<AllOrders> specification) {
        return allOrdersRepository.findAll(specification, pageable);
    }

    private void validateOrder(OrderDTO orderDTO, Stock stock, User user) {
        Map<String, List<String>> errors = new HashMap<>();
        if (orderDTO.getOrderType() == OrderType.BUYING_ORDER) {
            if (orderDTO.getPriceType() == PriceType.GREATER_OR_EQUAL) {
                errors.putIfAbsent("priceType", new ArrayList<>());
                errors.get("priceType").add("The buying order price's type cannot be GREATER_OR_EQUAL.");
            }
            if (orderDTO.getAmount() > stock.getAmount()) {
                errors.putIfAbsent("amount", new ArrayList<>());
                errors.get("amount").add("The given stock company does not have enough amount of action.");
            }
        } else {
            if (orderDTO.getPriceType() == PriceType.LESS_OR_EQUAL) {
                errors.putIfAbsent("priceType", new ArrayList<>());
                errors.get("priceType").add("The selling order price's type cannot be LESS_OR_EQUAL.");
            }
            Optional<Resource> resource = resourceRepository.findByUserAndStock(user, stock);
            if (resource.isEmpty() || resource.get().getAmount() < orderDTO.getAmount()) {
                errors.putIfAbsent("amount", new ArrayList<>());
                errors.get("amount").add("The logged in user does not have the specified amount of stocks.");
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidInputDataException("Data validation", errors);
        }
    }

}
