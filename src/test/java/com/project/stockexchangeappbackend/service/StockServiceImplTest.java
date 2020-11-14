package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.CreateStockDTO;
import com.project.stockexchangeappbackend.dto.OwnerDTO;
import com.project.stockexchangeappbackend.dto.StockDTO;
import com.project.stockexchangeappbackend.dto.UserDTO;
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

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.project.stockexchangeappbackend.service.OrderServiceImplTest.createCustomOrder;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.createCustomUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @InjectMocks
    StockServiceImpl stockService;

    @Mock
    StockRepository stockRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ArchivedOrderRepository archivedOrderRepository;

    @Mock
    ResourceRepository resourceRepository;

    @Mock
    StockIndexValueRepository stockIndexValueRepository;

    @Mock
    ModelMapper modelMapper;

    @Test
    void shouldReturnStockById() {
        Long id = 1L;
        Stock stock = createCustomStock(id, "WIG30", "WIG", 10000, BigDecimal.valueOf(100.20));
        when(stockRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(stock));
        assertStock(stockService.getStockById(id), stock);
    }

    @Test
    void shouldThrowEntityNotFoundWhenGettingStockById() {
        Long id = 1L;
        when(stockRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> stockService.getStockById(id));
    }

    @Test
    void shouldPageAndFilterStocks() {
        List<Stock> stocks = Arrays.asList(
                createCustomStock(1L, "WIG30", "W30", 10000, BigDecimal.valueOf(100.20)),
                createCustomStock(2L, "WIG20", "W20", 10000, BigDecimal.valueOf(10.20)));
        Pageable pageable = PageRequest.of(0,20);
        Specification<Stock> stockSpecification =
                (Specification<Stock>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");
        when(stockRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(stocks, pageable, stocks.size()));
        Page<Stock> output = stockService.getStocks(pageable, stockSpecification);
        assertEquals(stocks.size(), output.getNumberOfElements());
        for (int i=0; i<stocks.size(); i++) {
            assertStock(output.getContent().get(i), stocks.get(i));
        }
    }

    @Test
    void shouldReturnAllStocks() {
        List<Stock> stocks = Arrays.asList(
                createCustomStock(1L, "WIG30", "W30", 10000, BigDecimal.valueOf(100.20)),
                createCustomStock(2L, "WIG20", "W20", 10000, BigDecimal.valueOf(10.20)));
        when(stockRepository.findAll(Mockito.any(Specification.class))).thenReturn(stocks);
        List<Stock> output = stockService.getAllStocks();
        assertEquals(stocks.size(), output.size());
        for (int i=0; i<stocks.size(); i++) {
            assertStock(output.get(i), stocks.get(i));
        }
    }

    @Test
    void shouldUpdateStock() {
        Stock stock = createCustomStock(1L, "WIG30", "W30", 10000, BigDecimal.valueOf(100.20));
        when(stockRepository.save(stock)).thenReturn(stock);
        assertStock(stockService.updateStock(stock), stock);
    }

    @Test
    void shouldCreateStock() {
        OwnerDTO ownerDTO = createCustomOwnerDTO(100, 1L);
        CreateStockDTO createStockDTO = createCustomCreateStockDTO("WIG20", "W20",
                ownerDTO.getAmount(), BigDecimal.TEN, Collections.singletonList(ownerDTO));
        Stock stock = createCustomStock(null, createStockDTO.getName(), createStockDTO.getAbbreviation(),
                createStockDTO.getAmount(), createStockDTO.getCurrentPrice());
        User user = createCustomUser(ownerDTO.getUser().getId(), "test@test.com",
                "John", "Kowal", BigDecimal.ZERO, Role.USER);
        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(modelMapper.map(createStockDTO, Stock.class)).thenReturn(stock);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        assertAll(() -> stockService.createStock(createStockDTO));
    }

    @Test
    void shouldCreateStockWhenDeletedStockExistFoundByName() {
        OwnerDTO ownerDTO = createCustomOwnerDTO(100, 1L);
        CreateStockDTO createStockDTO = createCustomCreateStockDTO("WIG20", "W20",
                ownerDTO.getAmount(), BigDecimal.TEN, Collections.singletonList(ownerDTO));
        Stock stock = createCustomStock(null, createStockDTO.getName(), createStockDTO.getAbbreviation(),
                createStockDTO.getAmount(), createStockDTO.getCurrentPrice(), true);
        User user = createCustomUser(ownerDTO.getUser().getId(), "test@test.com",
                "John", "Kowal", BigDecimal.ZERO, Role.USER);
        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.of(stock));
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        assertAll(() -> stockService.createStock(createStockDTO));
    }

    @Test
    void shouldCreateStockWhenDeletedStockExistFoundByAbbreviation() {
        OwnerDTO ownerDTO = createCustomOwnerDTO(100, 1L);
        CreateStockDTO createStockDTO = createCustomCreateStockDTO("WIG20", "W20",
                ownerDTO.getAmount(), BigDecimal.TEN, Collections.singletonList(ownerDTO));
        Stock stock = createCustomStock(null, createStockDTO.getName(), createStockDTO.getAbbreviation(),
                createStockDTO.getAmount(), createStockDTO.getCurrentPrice(), true);
        User user = createCustomUser(ownerDTO.getUser().getId(), "test@test.com",
                "John", "Kowal", BigDecimal.ZERO, Role.USER);
        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation()))
                .thenReturn(Optional.of(stock));
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        assertAll(() -> stockService.createStock(createStockDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingStockAndAmountMismatch() {
        OwnerDTO ownerDTO = createCustomOwnerDTO(100, 1L);
        CreateStockDTO createStockDTO = createCustomCreateStockDTO("WIG20", "W20",
                ownerDTO.getAmount()*2, BigDecimal.TEN, Collections.singletonList(ownerDTO));
        Stock stock = createCustomStock(null, createStockDTO.getName(), createStockDTO.getAbbreviation(),
                createStockDTO.getAmount(), createStockDTO.getCurrentPrice());
        User user = createCustomUser(ownerDTO.getUser().getId(), "test@test.com",
                "John", "Kowal", BigDecimal.ZERO, Role.USER);
        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(modelMapper.map(createStockDTO, Stock.class)).thenReturn(stock);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> stockService.createStock(createStockDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenOneOfUsersNotFound() {
        OwnerDTO ownerDTO = createCustomOwnerDTO(100, 1L);
        CreateStockDTO createStockDTO = createCustomCreateStockDTO("WIG20", "W20",
                ownerDTO.getAmount()*2, BigDecimal.TEN, Collections.singletonList(ownerDTO));
        Stock stock = createCustomStock(null, createStockDTO.getName(), createStockDTO.getAbbreviation(),
                createStockDTO.getAmount(), createStockDTO.getCurrentPrice());
        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(modelMapper.map(createStockDTO, Stock.class)).thenReturn(stock);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> stockService.createStock(createStockDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenOneOfUsersIsAdmin() {
        OwnerDTO ownerDTO = createCustomOwnerDTO(100, 1L);
        CreateStockDTO createStockDTO = createCustomCreateStockDTO("WIG20", "W20",
                ownerDTO.getAmount()*2, BigDecimal.TEN, Collections.singletonList(ownerDTO));
        Stock stock = createCustomStock(null, createStockDTO.getName(), createStockDTO.getAbbreviation(),
                createStockDTO.getAmount(), createStockDTO.getCurrentPrice());
        User user = createCustomUser(ownerDTO.getUser().getId(), "test@test.com",
                "John", "Kowal", BigDecimal.ZERO, Role.ADMIN);
        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(modelMapper.map(createStockDTO, Stock.class)).thenReturn(stock);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> stockService.createStock(createStockDTO));
    }

    @Test
    void shouldThrowEntityAlreadyExistsExceptionWhenGivenAbbreviationFound() {
        OwnerDTO ownerDTO = createCustomOwnerDTO(100, 1L);
        CreateStockDTO createStockDTO = createCustomCreateStockDTO("WIG20", "W20",
                ownerDTO.getAmount()*2, BigDecimal.TEN, Collections.singletonList(ownerDTO));
        Stock stock = createCustomStock(1L, createStockDTO.getName(), createStockDTO.getAbbreviation(),
                createStockDTO.getAmount(), createStockDTO.getCurrentPrice(), false);
        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation()))
                .thenReturn(Optional.of(stock));

        assertThrows(EntityExistsException.class, () -> stockService.createStock(createStockDTO));
    }

    @Test
    void shouldThrowEntityAlreadyExistsExceptionWhenGivenNameFound() {
        OwnerDTO ownerDTO = createCustomOwnerDTO(100, 1L);
        CreateStockDTO createStockDTO = createCustomCreateStockDTO("WIG20", "W20",
                ownerDTO.getAmount()*2, BigDecimal.TEN, Collections.singletonList(ownerDTO));
        Stock stock = createCustomStock(1L, createStockDTO.getName(), createStockDTO.getAbbreviation(),
                createStockDTO.getAmount(), createStockDTO.getCurrentPrice(), false);
        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.of(stock));

        assertThrows(EntityExistsException.class, () -> stockService.createStock(createStockDTO));
    }

    @Test
    void shouldDeleteStock() {
        Long stockId = 1L;
        Stock stock = createCustomStock(stockId, "WIG30", "WIG", 10000, BigDecimal.valueOf(100.20));
        User user = createCustomUser(1L, "test@test.com", "John", "Kowal", BigDecimal.ZERO);
        List<Order> stocksOrders = List.of(createCustomOrder(1L, 100, 100,
                OrderType.SELLING_ORDER, PriceType.EQUAL, BigDecimal.TEN, OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().plusHours(1), null, user, stock));
        List<ArchivedOrder> archivedOrders = stocksOrders.stream()
                .map(OrderServiceImplTest::createCustomArchivedOrder)
                .collect(Collectors.toList());
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(orderRepository.findByStock(stock)).thenReturn(stocksOrders);
        when(modelMapper.map(Mockito.any(Order.class), Mockito.eq(ArchivedOrder.class)))
                .thenReturn(archivedOrders.get(0));
        assertAll(() -> stockService.deleteStock(stockId));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingStock() {
        Long stockId = 1L;
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> stockService.deleteStock(stockId));
    }

    public static void assertStock(Stock output, Stock expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getName(), output.getName()),
                () -> assertEquals(expected.getAbbreviation(), output.getAbbreviation()),
                () -> assertEquals(expected.getCurrentPrice(), output.getCurrentPrice()),
                () -> assertEquals(expected.getAmount(), output.getAmount()));
    }

    public static Stock createCustomStock(Long id, String name, String abbreviation,
                                            Integer amount, BigDecimal currentPrice) {
        return Stock.builder()
                .id(id).name(name).abbreviation(abbreviation)
                .amount(amount).currentPrice(currentPrice)
                .resources(new ArrayList<>())
                .build();
    }

    public static Stock createCustomStock(Long id, String name, String abbreviation,
                                          Integer amount, BigDecimal currentPrice, Boolean isDeleted) {
        return Stock.builder()
                .id(id).name(name).abbreviation(abbreviation)
                .amount(amount).currentPrice(currentPrice)
                .resources(new ArrayList<>())
                .isDeleted(isDeleted)
                .build();
    }

    public static StockDTO createCustomStockDTO(Long id, String name, String abbreviation,
                                                 Integer amount, BigDecimal currentPrice) {
        return StockDTO.builder()
                .id(id).name(name).abbreviation(abbreviation)
                .amount(amount).currentPrice(currentPrice)
                .build();
    }

    public static CreateStockDTO createCustomCreateStockDTO(String name, String abbreviation, Integer amount,
                                                            BigDecimal currentPrice, List<OwnerDTO> owners) {
        return CreateStockDTO.builder()
                .name(name).abbreviation(abbreviation)
                .amount(amount).currentPrice(currentPrice)
                .owners(owners)
                .build();
    }

    public static OwnerDTO createCustomOwnerDTO(Integer amount, Long userId) {
        return OwnerDTO.builder()
                .amount(amount)
                .user(UserDTO.builder().id(userId).build())
                .build();
    }

}
