package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.ResourceDTO;
import com.project.stockexchangeappbackend.entity.OrderType;
import com.project.stockexchangeappbackend.entity.Resource;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.repository.OrderRepository;
import com.project.stockexchangeappbackend.repository.ResourceRepository;
import com.project.stockexchangeappbackend.repository.UserRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.project.stockexchangeappbackend.service.StockServiceImplTest.createCustomStock;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.createCustomUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @InjectMocks
    ResourceServiceImpl resourceService;

    @Mock
    ResourceRepository resourceRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ModelMapper modelMapper;

    @Test
    void shouldPageAndFilterResources(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ONE);
        Stock stock = createCustomStock(1L, "WIG30", "W30", 100, BigDecimal.TEN);
        List<Resource> resources = Collections.singletonList(createCustomResource(1L, stock, user, 100));
        List<ResourceDTO> resourcesDTO = resources.stream()
                .map(res -> createCustomResourceDTO(res.getId(), res.getStock(), res.getAmount()))
                .collect(Collectors.toList());
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (Specification<Resource>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(resourceRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(resources, pageable, resources.size()));
        when(modelMapper.map(resources.get(0), ResourceDTO.class)).thenReturn(resourcesDTO.get(0));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(user), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        Page<ResourceDTO> output = resourceService.getOwnedResources(pageable, resourceSpecification);
        assertEquals(resourcesDTO.size(), output.getNumberOfElements());
        for (int i=0; i<resources.size(); i++) {
            assertResourceDTO(output.getContent().get(i), resourcesDTO.get(i));
        }
    }

    @Test
    void shouldPageAndFilterUsersResources() {
        Long userId = 1L;
        User user = createCustomUser(userId, "test@test.pl", "John", "Kowal", BigDecimal.ONE);
        Stock stock = createCustomStock(1L, "WIG30", "W30", 100, BigDecimal.TEN);
        List<Resource> resources = Collections.singletonList(createCustomResource(1L, stock, user, 100));
        List<ResourceDTO> resourcesDTO = resources.stream()
                .map(res -> createCustomResourceDTO(res.getId(), res.getStock(), res.getAmount()))
                .collect(Collectors.toList());
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (Specification<Resource>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(resourceRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(resources, pageable, resources.size()));
        when(modelMapper.map(resources.get(0), ResourceDTO.class)).thenReturn(resourcesDTO.get(0));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(user), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        Page<ResourceDTO> output = resourceService.getUsersResources(pageable, resourceSpecification, userId);
        assertEquals(resourcesDTO.size(), output.getNumberOfElements());
        for (int i=0; i<resources.size(); i++) {
            assertResourceDTO(output.getContent().get(i), resourcesDTO.get(i));
        }
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPagingAndFilteringUsersResources() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (Specification<Resource>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> resourceService.getUsersResources(pageable, resourceSpecification, userId));
    }

    public static void assertResourceDTO(ResourceDTO output, ResourceDTO expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertEquals(expected.getName(), output.getName()),
                () -> assertEquals(expected.getAbbreviation(), output.getAbbreviation()),
                () -> assertEquals(expected.getCurrentPrice(), output.getCurrentPrice()),
                () -> assertEquals(expected.getAmountAvailableForSale(), output.getAmountAvailableForSale()));
    }

    public static Resource createCustomResource (Long id, Stock stock, User user, Integer amount) {
        return Resource.builder()
                .id(id).stock(stock).user(user).amount(amount).build();
    }

    public static ResourceDTO createCustomResourceDTO (Long id, Stock stock, Integer amount) {
        return ResourceDTO.builder()
                .id(id)
                .name(stock.getName())
                .abbreviation(stock.getAbbreviation())
                .amount(amount)
                .amountAvailableForSale(amount)
                .currentPrice(stock.getCurrentPrice())
                .build();
    }

}