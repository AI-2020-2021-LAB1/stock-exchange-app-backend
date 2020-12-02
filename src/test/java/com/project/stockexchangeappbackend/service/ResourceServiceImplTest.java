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

import static com.project.stockexchangeappbackend.service.OrderServiceImplTest.createCustomSellingOrder;
import static com.project.stockexchangeappbackend.service.StockServiceImplTest.getStocksList;
import static com.project.stockexchangeappbackend.service.TagServiceImplTest.getTagsList;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.getUsersList;
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
    void shouldPageAndFilterOwnedResources(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        User user = getUsersList().get(0);
        List<Resource> resources = getStocksList().stream()
                .map(stock -> createCustomResource(1L, stock, user, stock.getAmount()))
                .collect(Collectors.toList());
        List<ResourceDTO> resourcesDTO = resources.stream()
                .map(res -> createCustomResourceDTO(res.getStock().getId(), res.getStock(), res.getAmount()))
                .collect(Collectors.toList());
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(resourceRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(resources, pageable, resources.size()));
        when(modelMapper.map(any(Resource.class), eq(ResourceDTO.class)))
                .thenReturn(resourcesDTO.get(0))
                .thenReturn(resourcesDTO.get(1));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                any(Stock.class), eq(user), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        Page<ResourceDTO> output = resourceService.getOwnedResources(pageable, resourceSpecification);
        assertEquals(resourcesDTO.size(), output.getNumberOfElements());
        for (int i=0; i<resources.size(); i++) {
            assertResourceDTO(output.getContent().get(i), resourcesDTO.get(i));
        }
    }

    @Test
    void shouldPageAndFilterUsersResources() {
        User user = getUsersList().get(0);
        List<Resource> resources = getStocksList().stream()
                .map(stock -> createCustomResource(1L, stock, user, stock.getAmount()))
                .collect(Collectors.toList());
        List<ResourceDTO> resourcesDTO = resources.stream()
                .map(res -> createCustomResourceDTO(res.getStock().getId(), res.getStock(), res.getAmount()))
                .collect(Collectors.toList());
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");
        Long userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(resourceRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(resources, pageable, resources.size()));
        when(modelMapper.map(any(Resource.class), eq(ResourceDTO.class)))
                .thenReturn(resourcesDTO.get(0))
                .thenReturn(resourcesDTO.get(1));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                any(Stock.class), eq(user), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
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
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> resourceService.getUsersResources(pageable, resourceSpecification, userId));
    }

    @Test
    void shouldPageAndFilterStockOwners() {
        Stock stock = getStocksList().get(0);
        Long stockId = stock.getId();
        List<Resource> resources = getUsersList().stream()
                .map(user -> (createCustomResource(1L, stock, user, stock.getAmount()/getUsersList().size())))
                .collect(Collectors.toList());
        List<OwnerDTO> ownersDTO = resources.stream()
                .map(res -> createCustomOwnerDTO(res.getUser(), res.getAmount()))
                .collect(Collectors.toList());
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), "user");

        when(stockRepository.findByIdAndIsDeletedFalse(stockId))
                .thenReturn(Optional.of(stock));
        when(resourceRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(resources, pageable, resources.size()));
        when(modelMapper.map(any(User.class), eq(UserDTO.class)))
                .thenReturn(ownersDTO.get(0).getUser())
                .thenReturn(ownersDTO.get(1).getUser());
        Page<OwnerDTO> output = resourceService.getStockOwners(pageable, resourceSpecification, stockId);
        assertEquals(ownersDTO.size(), output.getNumberOfElements());
        for (int i=0; i<ownersDTO.size(); i++) {
            assertOwnerDTO(output.getContent().get(i), ownersDTO.get(i));
        }
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPagingAndFilteringStockOwnersAndStockNotFound() {
        Long stockId = 1L;
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("email"), "test");

        when(stockRepository.findByIdAndIsDeletedFalse(stockId))
                .thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> resourceService.getStockOwners(pageable, resourceSpecification, stockId));
    }

    @Test
    void shouldMoveStocksFromOneToAnother() {
        Stock stock = getStocksList().get(0);
        List<User> users = getUsersList();
        users.get(1).setRole(Role.USER);
        MoveStockDTO moveStock =
                createRequestMoveStockDTO(users.get(0).getId(), users.get(1).getId(), stock.getAmount()/2);
        Resource resource = createCustomResource(1L, stock, users.get(0), stock.getAmount()/2);
        Long stockId = 1L;

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(moveStock.getUserSource().getId())).thenReturn(Optional.of(users.get(0)));
        when(userRepository.findById(moveStock.getUserDestination().getId())).thenReturn(Optional.of(users.get(1)));
        when(resourceRepository.findByUserAndStock(users.get(0), stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                any(Stock.class), any(User.class), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)
        )).thenReturn(Collections.emptyList());
        when(resourceRepository.findByUserAndStock(users.get(1),stock)).thenReturn(Optional.empty());
        assertAll(() -> resourceService.moveStock(stockId, moveStock));
        users.get(0).setRole(Role.USER);
    }

    @Test
    void shouldMoveStocksFromOneToAnotherNotAllUserSourceStocks() {
        Stock stock = getStocksList().get(0);
        List<User> users = getUsersList();
        users.get(1).setRole(Role.USER);
        MoveStockDTO moveStock =
                createRequestMoveStockDTO(users.get(0).getId(), users.get(1).getId(), stock.getAmount()/4);
        Resource resource = createCustomResource(1L, stock, users.get(0), stock.getAmount()/2);
        Long stockId = 1L;

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(moveStock.getUserSource().getId())).thenReturn(Optional.of(users.get(0)));
        when(userRepository.findById(moveStock.getUserDestination().getId())).thenReturn(Optional.of(users.get(1)));
        when(resourceRepository.findByUserAndStock(users.get(0), stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                any(Stock.class), any(User.class), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)
        )).thenReturn(Collections.emptyList());
        when(resourceRepository.findByUserAndStock(users.get(1),stock)).thenReturn(Optional.empty());
        assertAll(() -> resourceService.moveStock(stockId, moveStock));
        users.get(0).setRole(Role.USER);
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndSourceUserNotHaveEnoughStock() {
        Stock stock = getStocksList().get(0);
        List<User> users = getUsersList();
        users.get(1).setRole(Role.USER);
        MoveStockDTO moveStock =
                createRequestMoveStockDTO(users.get(0).getId(), users.get(1).getId(), stock.getAmount()/2);
        Resource resource = createCustomResource(1L, stock, users.get(0), stock.getAmount()/2);
        Order order = createCustomSellingOrder(1L, stock.getAmount()/4, BigDecimal.ONE,
                OffsetDateTime.now().plusHours(1), users.get(0), stock);
        Long stockId = 1L;

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(moveStock.getUserSource().getId())).thenReturn(Optional.of(users.get(0)));
        when(userRepository.findById(moveStock.getUserDestination().getId())).thenReturn(Optional.of(users.get(1)));
        when(resourceRepository.findByUserAndStock(users.get(0), stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(users.get(0)), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.singletonList(order));
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
        users.get(1).setRole(Role.ADMIN);
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndSourceUserIsAdmin() {
        Stock stock = getStocksList().get(0);
        List<User> users = getUsersList();
        MoveStockDTO moveStock =
                createRequestMoveStockDTO(users.get(0).getId(), users.get(1).getId(), stock.getAmount()/2);
        Resource resource = createCustomResource(1L, stock, users.get(0), stock.getAmount()/2);
        Long stockId = 1L;

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(moveStock.getUserSource().getId())).thenReturn(Optional.of(users.get(0)));
        when(userRepository.findById(moveStock.getUserDestination().getId())).thenReturn(Optional.of(users.get(1)));
        when(resourceRepository.findByUserAndStock(users.get(0), stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(users.get(0)), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndOthersTags() {
        Stock stock = getStocksList().get(0);
        List<User> users = getUsersList();
        users.get(1).setRole(Role.USER);
        users.get(1).setTag(getTagsList().get(1));
        MoveStockDTO moveStock =
                createRequestMoveStockDTO(users.get(0).getId(), users.get(1).getId(), stock.getAmount()/2);
        Resource resource = createCustomResource(1L, stock, users.get(0), stock.getAmount()/2);
        Long stockId = 1L;

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(moveStock.getUserSource().getId())).thenReturn(Optional.of(users.get(0)));
        when(userRepository.findById(moveStock.getUserDestination().getId())).thenReturn(Optional.of(users.get(1)));
        when(resourceRepository.findByUserAndStock(users.get(0), stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(users.get(0)), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class))).
                thenReturn(Collections.emptyList());
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
        users.get(1).setRole(Role.ADMIN);
        users.get(1).setTag(getTagsList().get(0));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndSourceUserIsDestination() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        MoveStockDTO moveStock =
                createRequestMoveStockDTO(user.getId(), user.getId(), stock.getAmount()/2);
        Resource resource = createCustomResource(1L, stock, user, stock.getAmount()/2);
        Long stockId = 1L;

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(moveStock.getUserSource().getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(moveStock.getUserDestination().getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        when(orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                eq(stock), eq(user), eq(OrderType.SELLING_ORDER), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndDestinationUserNotFound() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        MoveStockDTO moveStock =
                createRequestMoveStockDTO(user.getId(), 2L, stock.getAmount()/2);
        Long stockId = 1L;

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(moveStock.getUserSource().getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(moveStock.getUserDestination().getId())).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenMovingStocksFromOneToAnotherAndSourceUserNotFound() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        MoveStockDTO moveStock =
                createRequestMoveStockDTO(user.getId(), 2L, stock.getAmount()/2);
        Long stockId = 1L;

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(moveStock.getUserSource().getId())).thenReturn(Optional.empty());
        when(userRepository.findById(moveStock.getUserDestination().getId())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> resourceService.moveStock(stockId, moveStock));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenMovingStocksFromOneToAnotherAndStockNotFound() {
        MoveStockDTO moveStock =
                createRequestMoveStockDTO(1L, 2L, 100);
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

    public static MoveStockDTO createRequestMoveStockDTO (Long sourceId, Long destinationId, Integer amount) {
        return MoveStockDTO.builder()
                .amount(amount)
                .userSource(UserDTO.builder().id(sourceId).build())
                .userDestination(UserDTO.builder().id(destinationId).build())
                .build();
    }

}
