package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.MoveStockDTO;
import com.project.stockexchangeappbackend.dto.OwnerDTO;
import com.project.stockexchangeappbackend.dto.ResourceDTO;
import com.project.stockexchangeappbackend.dto.UserDTO;
import com.project.stockexchangeappbackend.entity.*;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.OrderRepository;
import com.project.stockexchangeappbackend.repository.ResourceRepository;
import com.project.stockexchangeappbackend.repository.StockRepository;
import com.project.stockexchangeappbackend.repository.UserRepository;
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

import static com.project.stockexchangeappbackend.service.OrderServiceImplTest.createCustomOrder;
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
    StockRepository stockRepository;

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

    @Test
    void shouldPageAndFilterStockOwners() {
        Long stockId = 1L;
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ONE);
        Stock stock = createCustomStock(stockId, "WIG30", "W30", 100, BigDecimal.TEN);
        List<Resource> resources = Collections.singletonList(createCustomResource(1L, stock, user, 100));
        List<OwnerDTO> ownersDTO = resources.stream()
                .map(res -> createCustomOwnerDTO(res.getUser(), res.getAmount()))
                .collect(Collectors.toList());
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (Specification<Resource>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), "test");
        when(stockRepository.findByIdAndIsDeletedFalse(stockId))
                .thenReturn(Optional.of(stock));
        when(resourceRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(resources, pageable, resources.size()));
        when(modelMapper.map(resources.get(0).getUser(), UserDTO.class)).thenReturn(ownersDTO.get(0).getUser());
        Page<OwnerDTO> output = resourceService.getStockOwners(pageable, resourceSpecification, stockId);
        assertEquals(ownersDTO.size(), output.getNumberOfElements());
        for (int i=0; i<ownersDTO.size(); i++) {
            assertOwnerDTO(output.getContent().get(i), ownersDTO.get(i));
        }
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWHenPagingAndFilteringStockOwnersAndStockNotFound() {
        Long stockId = 1L;
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (Specification<Resource>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), "test");
        when(stockRepository.findByIdAndIsDeletedFalse(stockId))
                .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> resourceService.getStockOwners(pageable, resourceSpecification, stockId));
    }

    @Test
    void shouldMoveStocksFromOneToAnother() {
        MoveStockDTO moveStock = MoveStockDTO.builder()
                .amount(100)
                .userDestination(UserDTO.builder().id(1L).build())
                .userSource(UserDTO.builder().id(2L).build())
                .build();
        Long stockId = 1L;
        Tag tag = new Tag(1L, "default");
        Stock stock = createCustomStock(1L, "Wig20", "W20", 100, BigDecimal.TEN, tag);
        User user = createCustomUser(1L, "test@test", "test", "test", BigDecimal.ZERO, tag);
        User user2 = createCustomUser(2L, "test2@test", "test", "test", BigDecimal.ZERO, tag);
        Resource resource = createCustomResource(1L, stock, user2, 100);
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(resourceRepository.findByUserAndStock(user2, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                Mockito.eq(stock), Mockito.eq(user2), Mockito.eq(OrderType.SELLING_ORDER), Mockito.any(OffsetDateTime.class)
        )).thenReturn(Collections.emptyList());
        when(resourceRepository.findByUserAndStock(user,stock)).thenReturn(Optional.empty());
        assertAll(() -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndSourceUserNotHaveEnoughStock() {
        MoveStockDTO moveStock = MoveStockDTO.builder()
                .amount(100)
                .userDestination(UserDTO.builder().id(1L).build())
                .userSource(UserDTO.builder().id(2L).build())
                .build();
        Long stockId = 1L;
        Tag tag = new Tag(1L, "default");
        Stock stock = createCustomStock(1L, "Wig20", "W20", 100, BigDecimal.TEN, tag);
        User user = createCustomUser(1L, "test@test", "test", "test", BigDecimal.ZERO, tag);
        User user2 = createCustomUser(2L, "test2@test", "test", "test", BigDecimal.ZERO, tag);
        Resource resource = createCustomResource(1L, stock, user2, 100);
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(resourceRepository.findByUserAndStock(user2, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                Mockito.eq(stock), Mockito.eq(user2), Mockito.eq(OrderType.SELLING_ORDER), Mockito.any(OffsetDateTime.class)
        )).thenReturn(Collections.singletonList(
                createCustomOrder(1L, 10, 10, OrderType.SELLING_ORDER, PriceType.EQUAL,
                        BigDecimal.ONE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusHours(1),
                        null, user2, stock)));
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndSourceUserIsAdmin() {
        MoveStockDTO moveStock = MoveStockDTO.builder()
                .amount(100)
                .userDestination(UserDTO.builder().id(1L).build())
                .userSource(UserDTO.builder().id(2L).build())
                .build();
        Long stockId = 1L;
        Tag tag = new Tag(1L, "default");
        Stock stock = createCustomStock(1L, "Wig20", "W20", 100, BigDecimal.TEN, tag);
        User user = createCustomUser(1L, "test@test", "test", "test", BigDecimal.ZERO, Role.ADMIN, tag);
        User user2 = createCustomUser(2L, "test2@test", "test", "test", BigDecimal.ZERO, tag);
        Resource resource = createCustomResource(1L, stock, user2, 100);
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(resourceRepository.findByUserAndStock(user2, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                Mockito.eq(stock), Mockito.eq(user2), Mockito.eq(OrderType.SELLING_ORDER), Mockito.any(OffsetDateTime.class)
        )).thenReturn(Collections.singletonList(
                createCustomOrder(1L, 10, 10, OrderType.SELLING_ORDER, PriceType.EQUAL,
                        BigDecimal.ONE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusHours(1),
                        null, user2, stock)));
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndOthersTags() {
        MoveStockDTO moveStock = MoveStockDTO.builder()
                .amount(100)
                .userDestination(UserDTO.builder().id(1L).build())
                .userSource(UserDTO.builder().id(2L).build())
                .build();
        Long stockId = 1L;
        Tag tag = new Tag(1L, "default");
        Tag tag2 = new Tag(2L, "default");
        Stock stock = createCustomStock(1L, "Wig20", "W20", 100, BigDecimal.TEN, tag);
        User user = createCustomUser(1L, "test@test", "test", "test", BigDecimal.ZERO, tag);
        User user2 = createCustomUser(2L, "test2@test", "test", "test", BigDecimal.ZERO, tag2);
        Resource resource = createCustomResource(1L, stock, user2, 100);
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(resourceRepository.findByUserAndStock(user2, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                Mockito.eq(stock), Mockito.eq(user2), Mockito.eq(OrderType.SELLING_ORDER), Mockito.any(OffsetDateTime.class)
        )).thenReturn(Collections.emptyList());
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndSourceUserIsDestination() {
        MoveStockDTO moveStock = MoveStockDTO.builder()
                .amount(100)
                .userDestination(UserDTO.builder().id(2L).build())
                .userSource(UserDTO.builder().id(2L).build())
                .build();
        Long stockId = 1L;
        Tag tag = new Tag(1L, "default");
        Stock stock = createCustomStock(1L, "Wig20", "W20", 100, BigDecimal.TEN, tag);
        User user2 = createCustomUser(2L, "test2@test", "test", "test", BigDecimal.ZERO, tag);
        Resource resource = createCustomResource(1L, stock, user2, 100);
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(resourceRepository.findByUserAndStock(user2, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                Mockito.eq(stock), Mockito.eq(user2), Mockito.eq(OrderType.SELLING_ORDER), Mockito.any(OffsetDateTime.class)
        )).thenReturn(Collections.emptyList());
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndDestinationUserNotFound() {
        MoveStockDTO moveStock = MoveStockDTO.builder()
                .amount(100)
                .userDestination(UserDTO.builder().id(1L).build())
                .userSource(UserDTO.builder().id(2L).build())
                .build();
        Long stockId = 1L;
        Tag tag = new Tag(1L, "default");
        Stock stock = createCustomStock(1L, "Wig20", "W20", 100, BigDecimal.TEN, tag);
        User user = createCustomUser(1L, "test@test", "test", "test", BigDecimal.ZERO, tag);
        User user2 = createCustomUser(2L, "test2@test", "test", "test", BigDecimal.ZERO, tag);
        Resource resource = createCustomResource(1L, stock, user2, 100);
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        when(resourceRepository.findByUserAndStock(user2, stock)).thenReturn(Optional.of(resource));
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndSourceUserNotFound() {
        MoveStockDTO moveStock = MoveStockDTO.builder()
                .amount(100)
                .userDestination(UserDTO.builder().id(1L).build())
                .userSource(UserDTO.builder().id(2L).build())
                .build();
        Long stockId = 1L;
        Tag tag = new Tag(1L, "default");
        Stock stock = createCustomStock(1L, "Wig20", "W20", 100, BigDecimal.TEN, tag);
        User user = createCustomUser(1L, "test@test", "test", "test", BigDecimal.ZERO, tag);
        User user2 = createCustomUser(2L, "test2@test", "test", "test", BigDecimal.ZERO, tag);
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenMovingStocksFromOneToAnotherAndStockNotFound() {
        MoveStockDTO moveStock = MoveStockDTO.builder()
                .amount(100)
                .userDestination(UserDTO.builder().id(1L).build())
                .userSource(UserDTO.builder().id(2L).build())
                .build();
        Long stockId = 1L;
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    public static void assertResourceDTO(ResourceDTO output, ResourceDTO expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertEquals(expected.getName(), output.getName()),
                () -> assertEquals(expected.getAbbreviation(), output.getAbbreviation()),
                () -> assertEquals(expected.getCurrentPrice(), output.getCurrentPrice()),
                () -> assertEquals(expected.getAmountAvailableForSale(), output.getAmountAvailableForSale()));
    }

    public static void assertOwnerDTO(OwnerDTO output, OwnerDTO expected) {
        assertAll(() -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertUserDTO(expected.getUser(), output.getUser()));
    }

    private static void assertUserDTO(UserDTO expected, UserDTO output) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getEmail(), output.getEmail()),
                () -> assertEquals(expected.getFirstName(), output.getFirstName()),
                () -> assertEquals(expected.getLastName(), output.getLastName()),
                () -> assertEquals(expected.getMoney(), output.getMoney()));
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

    public static OwnerDTO createCustomOwnerDTO (User user, Integer amount) {
        return OwnerDTO.builder()
                .amount(amount)
                .user(UserDTO.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .money(user.getMoney())
                        .build())
                .build();
    }

}
