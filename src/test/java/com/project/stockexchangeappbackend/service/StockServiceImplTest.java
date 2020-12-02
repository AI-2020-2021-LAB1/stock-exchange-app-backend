package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.*;
import com.project.stockexchangeappbackend.entity.*;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
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
import static com.project.stockexchangeappbackend.service.TagServiceImplTest.assertTag;
import static com.project.stockexchangeappbackend.service.TagServiceImplTest.getTagsList;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.createCustomUser;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.getUsersList;
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
    TagService tagService;

    @Mock
    ModelMapper modelMapper;

    @BeforeEach
    void setup() {
        setStockList();
    }

    @Test
    void shouldReturnStockById() {
        Stock stock = getStocksList().get(0);
        Long id = stock.getId();
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
        List<Stock> stocks = getStocksList();
        Pageable pageable = PageRequest.of(0,20);
        Specification<Stock> stockSpecification =
                (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");

        when(stockRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(stocks, pageable, stocks.size()));
        Page<Stock> output = stockService.getStocks(pageable, stockSpecification);
        assertEquals(stocks.size(), output.getNumberOfElements());
        for (int i=0; i<stocks.size(); i++) {
            assertStock(output.getContent().get(i), stocks.get(i));
        }
    }

    @Test
    void shouldReturnStockByAbbreviation() {
        Stock stock = getStocksList().get(0);
        String abbreviation = stock.getAbbreviation();
        when(stockRepository.findByAbbreviationIgnoreCaseAndIsDeletedFalse(abbreviation)).thenReturn(Optional.of(stock));
        assertStock(stockService.getStockByAbbreviation(abbreviation), stock);
    }

    @Test
    void shouldThrowEntityNotFoundWhenGettingStockByAbbreviation() {
        String abbreviation = "non";
        when(stockRepository.findByAbbreviationIgnoreCaseAndIsDeletedFalse(abbreviation)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> stockService.getStockByAbbreviation(abbreviation));
    }

    @Test
    void shouldReturnAllStocks() {
        List<Stock> stocks = getStocksList();

        when(stockRepository.findAll(Mockito.any(Specification.class))).thenReturn(stocks);
        List<Stock> output = stockService.getAllStocks();
        assertEquals(stocks.size(), output.size());
        for (int i=0; i<stocks.size(); i++) {
            assertStock(output.get(i), stocks.get(i));
        }
    }

    @Test
    void shouldUpdateStockObject() {
        Stock stock = getStocksList().get(0);
        when(stockRepository.save(stock)).thenReturn(stock);
        assertStock(stockService.updateStock(stock), stock);
    }

    @Test
    void shouldUpdateStockDetails() {
        Stock stock = getStocksList().get(0);
        StockDTO stockDTO = StockDTO.builder().name("new Name").abbreviation("nam").build();
        String id = stock.getId().toString();

        when(stockRepository.findByIdAndIsDeletedFalse(Long.valueOf(id))).thenReturn(Optional.of(stock));
        when(stockRepository.findByAbbreviationIgnoreCase(stockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(stockRepository.findByNameIgnoreCase(stockDTO.getName())).thenReturn(Optional.empty());
        assertAll(() -> stockService.updateStock(stockDTO, id));
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenUpdatingStockDetailsAndAbbreviationExist() {
        Stock stock = getStocksList().get(0);
        Stock stock2 = getStocksList().get(1);
        StockDTO stockDTO = StockDTO.builder().name("new Name").abbreviation(stock2.getAbbreviation()).build();
        String id = stock.getId().toString();

        when(stockRepository.findByIdAndIsDeletedFalse(Long.valueOf(id))).thenReturn(Optional.of(stock));
        when(stockRepository.findByAbbreviationIgnoreCase(stockDTO.getAbbreviation())).thenReturn(Optional.of(stock2));
        assertThrows(EntityExistsException.class, () -> stockService.updateStock(stockDTO, id));
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenUpdatingStockDetailsAndNameExist() {
        Stock stock = getStocksList().get(0);
        Stock stock2 = getStocksList().get(1);
        StockDTO stockDTO = StockDTO.builder().name(stock2.getName()).abbreviation("nam").build();
        String id = stock.getId().toString();

        when(stockRepository.findByIdAndIsDeletedFalse(Long.valueOf(id))).thenReturn(Optional.of(stock));
        when(stockRepository.findByAbbreviationIgnoreCase(stockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(stockRepository.findByNameIgnoreCase(stockDTO.getName())).thenReturn(Optional.of(stock2));
        assertThrows(EntityExistsException.class, () -> stockService.updateStock(stockDTO, id));
    }

    @Test
    void shouldReturnStockByIdOrAbbreviation() {
        Stock stock = getStocksList().get(0);
        String id = stock.getId().toString();
        when(stockRepository.findByIdAndIsDeletedFalse(Long.valueOf(id))).thenReturn(Optional.of(stock));
        assertStock(stockService.getStockByIdOrAbbreviation(id), stock);
    }

    @Test
    void shouldReturnStockByIdOrAbbreviation2() {
        Stock stock = getStocksList().get(0);
        String id = stock.getName();
        when(stockRepository.findByAbbreviationIgnoreCaseAndIsDeletedFalse(id)).thenReturn(Optional.of(stock));
        assertStock(stockService.getStockByIdOrAbbreviation(id), stock);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingStockByIdOrAbbreviation() {
        Stock stock = getStocksList().get(0);
        String id = stock.getId().toString();
        when(stockRepository.findByIdAndIsDeletedFalse(Long.valueOf(id))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> stockService.getStockByIdOrAbbreviation(id));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingStockByIdOrAbbreviation2() {
        Stock stock = getStocksList().get(0);
        String id = stock.getName();
        when(stockRepository.findByAbbreviationIgnoreCaseAndIsDeletedFalse(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> stockService.getStockByIdOrAbbreviation(id));
    }

    @Test
    void shouldCreateStock() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateStockDTO createStockDTO = createRequestCreateStockDTO(stock, user);
        Tag tag = stock.getTag();

        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(modelMapper.map(createStockDTO, Stock.class)).thenReturn(stock);
        when(tagService.getTag(tag.getName())).thenReturn(tag);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        assertAll(() -> stockService.createStock(createStockDTO, tag.getName()));
    }

    @Test
    void shouldCreateStockWhenDeletedStockExistFoundByName() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateStockDTO createStockDTO = createRequestCreateStockDTO(stock, user);
        Tag tag = stock.getTag();
        stock.setIsDeleted(Boolean.TRUE);

        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.of(stock));
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(tagService.getTag(tag.getName())).thenReturn(tag);
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));
        assertAll(() -> stockService.createStock(createStockDTO, tag.getName()));
    }

    @Test
    void shouldCreateStockWhenDeletedStockExistFoundByAbbreviation() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateStockDTO createStockDTO = createRequestCreateStockDTO(stock, user);
        Tag tag = stock.getTag();
        stock.setIsDeleted(Boolean.TRUE);

        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation()))
                .thenReturn(Optional.of(stock));
        when(tagService.getTag(tag.getName())).thenReturn(tag);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        assertAll(() -> stockService.createStock(createStockDTO, tag.getName()));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenCreatingStockAndAmountMismatch() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateStockDTO createStockDTO = createRequestCreateStockDTO(stock, user);
        Tag tag = stock.getTag();
        createStockDTO.setAmount(createStockDTO.getAmount()*2);

        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(modelMapper.map(createStockDTO, Stock.class)).thenReturn(stock);
        when(tagService.getTag(tag.getName())).thenReturn(tag);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> stockService.createStock(createStockDTO, tag.getName()));
        createStockDTO.setAmount(createStockDTO.getAmount()/2);
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenOneOfUsersNotFound() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateStockDTO createStockDTO = createRequestCreateStockDTO(stock, user);
        Tag tag = stock.getTag();

        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(modelMapper.map(createStockDTO, Stock.class)).thenReturn(stock);
        when(tagService.getTag(tag.getName())).thenReturn(tag);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> stockService.createStock(createStockDTO, tag.getName()));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenOneOfUsersIsAdmin() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(1);
        CreateStockDTO createStockDTO = createRequestCreateStockDTO(stock, user);
        Tag tag = stock.getTag();

        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(modelMapper.map(createStockDTO, Stock.class)).thenReturn(stock);
        when(tagService.getTag(tag.getName())).thenReturn(tag);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> stockService.createStock(createStockDTO, tag.getName()));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenOneOfUsersIsTaggedUsingAnotherTagThanStock() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateStockDTO createStockDTO = createRequestCreateStockDTO(stock, user);
        Tag tag2 = getTagsList().get(1);

        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation())).thenReturn(Optional.empty());
        when(modelMapper.map(createStockDTO, Stock.class)).thenReturn(stock);
        when(tagService.getTag(tag2.getName())).thenReturn(tag2);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> stockService.createStock(createStockDTO, tag2.getName()));
    }

    @Test
    void shouldThrowEntityAlreadyExistsExceptionWhenGivenAbbreviationFound() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateStockDTO createStockDTO = createRequestCreateStockDTO(stock, user);
        Tag tag = stock.getTag();

        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.empty());
        when(stockRepository.findByAbbreviationIgnoreCase(createStockDTO.getAbbreviation()))
                .thenReturn(Optional.of(stock));
        assertThrows(EntityExistsException.class, () -> stockService.createStock(createStockDTO, tag.getName()));
    }

    @Test
    void shouldThrowEntityAlreadyExistsExceptionWhenGivenNameFound() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        CreateStockDTO createStockDTO = createRequestCreateStockDTO(stock, user);
        Tag tag = stock.getTag();
        when(stockRepository.findByNameIgnoreCase(createStockDTO.getName())).thenReturn(Optional.of(stock));
        assertThrows(EntityExistsException.class, () -> stockService.createStock(createStockDTO, tag.getName()));
    }

    @Test
    void shouldDeleteStock() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        stock.setResources(new ArrayList<>(
                Collections.singletonList(
                        Resource.builder().stock(stock).user(user).amount(stock.getAmount()).build()
                )));
        Long stockId = stock.getId();
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
        stock.getResources().clear();
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingStock() {
        Long stockId = 1L;
        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> stockService.deleteStock(stockId));
    }

    @Test
    void shouldUpdateStockAmount() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount());
        Long stockId = stock.getId();

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.empty());
        assertAll(() -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldUpdateStockAmountWhenUserAlreadyPossessUpdatingStockAddingStock() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount());
        Long stockId = stock.getId();
        Resource resource = Resource.builder().id(1L).stock(stock).user(user).amount(10).build();
        stock.getResources().add(resource);

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        assertAll(() -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldUpdateStockAmountWhenUserAlreadyPossessUpdatingStockDeletingStock() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount()/2);
        updateStockAmount.setAmount(updateStockAmount.getAmount()*-1);
        Resource resource = Resource.builder().id(1L).stock(stock).user(user).amount(stock.getAmount()/2).build();
        stock.getResources().add(resource);
        user.setTag(stock.getTag());
        Long stockId = stock.getId();

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.of(resource));
        assertAll(() -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingStockAmountAndUserNotOwnStock() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount()/2);
        updateStockAmount.setAmount(-stock.getAmount()/2);
        Long stockId = stock.getId();

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(resourceRepository.findByUserAndStock(user, stock)).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingStockAmountAndUserTaggedUsingAnotherTag() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount());
        Long stockId = stock.getId();
        user.setTag(getTagsList().get(1));

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingStockAmountAndUserIsAdmin() {
        Stock stock = getStocksList().get(0);
        User user = getUsersList().get(1);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount());
        updateStockAmount.getOwners().get(0).getUser().setId(user.getId());
        Long stockId = stock.getId();

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingStockAmountAndUserNotFound() {
        Stock stock = getStocksList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount());
        Long stockId = stock.getId();

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingStockAmountAndNonPositiveValueAfterChange() {
        Stock stock = getStocksList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount()*2);
        updateStockAmount.setAmount(updateStockAmount.getAmount()*-1);
        Long stockId = stock.getId();

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        assertThrows(InvalidInputDataException.class, () -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingStockAmountAndAmountValuesMismatch() {
        Stock stock = getStocksList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount());
        updateStockAmount.setAmount(updateStockAmount.getAmount()/2);
        Long stockId = stock.getId();

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        assertThrows(InvalidInputDataException.class, () -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingStockAmountAndAmountIsZero() {
        Stock stock = getStocksList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(0);
        Long stockId = stock.getId();

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.of(stock));
        assertThrows(InvalidInputDataException.class, () -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUpdatingStockAmountAndStockNotFound() {
        Stock stock = getStocksList().get(0);
        UpdateStockAmountDTO updateStockAmount = createRequestUpdateStockAmountDTO(stock.getAmount());
        updateStockAmount.setAmount(updateStockAmount.getAmount()/2);
        Long stockId = 3L;

        when(stockRepository.findByIdAndIsDeletedFalse(stockId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> stockService.updateStockAmount(stockId, updateStockAmount));
    }

    public static List<Stock> stocks;

    public static List<Stock> getStocksList() {
        if (stocks == null) {
            setStockList();
        }
        return stocks;
    }

    private static void setStockList() {
        var tags = getTagsList();
        stocks = Arrays.asList(
                Stock.builder()
                        .id(1L).name("WiG20").abbreviation("W20").amount(10000).currentPrice(BigDecimal.ZERO)
                        .tag(tags.get(0)).isDeleted(Boolean.FALSE).resources(new ArrayList<>())
                        .build(),
                Stock.builder()
                        .id(2L).name("WiG30").abbreviation("W30").amount(10000).currentPrice(BigDecimal.ZERO)
                        .tag(tags.get(0)).isDeleted(Boolean.FALSE).resources(new ArrayList<>())
                        .build());
    }

    public static void assertStock(Stock output, Stock expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getName(), output.getName()),
                () -> assertEquals(expected.getAbbreviation(), output.getAbbreviation()),
                () -> assertEquals(expected.getCurrentPrice(), output.getCurrentPrice()),
                () -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertTag(expected.getTag(), output.getTag()));
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
                                          Integer amount, BigDecimal currentPrice, Tag tag) {
        return Stock.builder()
                .id(id).name(name).abbreviation(abbreviation)
                .amount(amount).currentPrice(currentPrice)
                .resources(new ArrayList<>())
                .tag(tag)
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

    public static Stock createCustomStock(Long id, String name, String abbreviation,
                                          Integer amount, BigDecimal currentPrice, Boolean isDeleted, Tag tag) {
        return Stock.builder()
                .id(id).name(name).abbreviation(abbreviation)
                .amount(amount).currentPrice(currentPrice)
                .resources(new ArrayList<>())
                .isDeleted(isDeleted)
                .tag(tag)
                .build();
    }

    public static StockDTO createCustomStockDTO(Long id, String name, String abbreviation,
                                                 Integer amount, BigDecimal currentPrice) {
        return StockDTO.builder()
                .id(id).name(name).abbreviation(abbreviation)
                .amount(amount).currentPrice(currentPrice)
                .build();
    }

    public static CreateStockDTO createRequestCreateStockDTO(Stock stock, User user) {
        return CreateStockDTO.builder()
                .name(stock.getName())
                .abbreviation(stock.getAbbreviation())
                .amount(stock.getAmount())
                .currentPrice(BigDecimal.TEN)
                .owners(Collections.singletonList(
                        OwnerDTO.builder()
                                .amount(stock.getAmount())
                                .user(UserDTO.builder().id(user.getId()).build())
                                .build()))
                .build();
    }

    public static UpdateStockAmountDTO createRequestUpdateStockAmountDTO(Integer amount) {
        return UpdateStockAmountDTO.builder()
                .amount(amount)
                .owners(Collections.singletonList(
                        OwnerDTO.builder()
                                .amount(amount)
                                .user(UserDTO.builder().id(1L).build())
                                .build()
                )).build();
    }

}
