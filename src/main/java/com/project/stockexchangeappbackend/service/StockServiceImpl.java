package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.CreateStockDTO;
import com.project.stockexchangeappbackend.dto.OwnerDTO;
import com.project.stockexchangeappbackend.dto.StockDTO;
import com.project.stockexchangeappbackend.entity.*;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.*;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ArchivedOrderRepository archivedOrderRepository;
    private final ResourceRepository resourceRepository;
    private final StockIndexValueRepository stockIndexValueRepository;
    private final TagService tagService;
    private final ModelMapper modelMapper;

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Stock getStockById(Long id) {
        return stockRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Stock Not Found"));
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Page<Stock> getStocks(Pageable pageable, Specification<Stock> specification) {
        Specification<Stock> stockNotDeleted = (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), false);
        return stockRepository.findAll(Specification.where(stockNotDeleted).and(specification), pageable);
    }

    @Override
    @LogicBusinessMeasureTime
    public Stock getStockByAbbreviation(String abbreviation) {
        return stockRepository.findByAbbreviationIgnoreCaseAndIsDeletedFalse(abbreviation)
                .orElseThrow(() -> new EntityNotFoundException("Stock Not Found"));
    }

    @Override
    @LogicBusinessMeasureTime
    public List<Stock> getAllStocks() {
        Specification<Stock> stockNotDeleted = (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), false);
        return stockRepository.findAll(Specification.where(stockNotDeleted));
    }

    @Override
    @LogicBusinessMeasureTime
    public Stock updateStock(Stock stock) {
        return stockRepository.save(stock);
    }

    @Transactional
    @LogicBusinessMeasureTime
    public void updateStock(StockDTO stockDTO, String id) {
        Stock stock = getStockByIdOrAbbreviation(id);
        Optional<Stock> stockByAbbreviation = stockRepository.findByAbbreviationIgnoreCase(stockDTO.getAbbreviation().trim());
        if (stockByAbbreviation.isPresent() && !stock.getId().equals(stockByAbbreviation.get().getId())) {
            throw new EntityExistsException("Stock with given abbreviation already exists");
        }
        Optional<Stock> stockByName = stockRepository.findByNameIgnoreCase(stockDTO.getName().trim());
        if (stockByName.isPresent() && !stock.getId().equals(stockByName.get().getId())) {
            throw new EntityExistsException("Stock with given name already exists");
        }
        stock.setAbbreviation(stockDTO.getAbbreviation().trim());
        stock.setName(stockDTO.getName().trim());
        stockRepository.save(stock);
    }

    @Override
    @LogicBusinessMeasureTime
    public Stock getStockByIdOrAbbreviation(String id) {
        try {
            return getStockById(Long.valueOf(id));
        } catch (NumberFormatException e) {
            return getStockByAbbreviation(id);
        }
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional
    public void createStock(CreateStockDTO stockDTO, String tag) {
        Stock stock = validateCreateStockDTO(stockDTO, tag);
        stock.setResources(stock.getResources().stream()
                .collect(Collectors.groupingBy(Resource::getUser))
                .values().stream()
                .map(res -> {
                    res.get(0).setAmount(res.stream().mapToInt(Resource::getAmount).sum());
                    return res.get(0);
                }).collect(Collectors.toList()));
        stockRepository.save(stock);
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional
    public void deleteStock(Long id) {
        Stock stock = stockRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));
        stock.setIsDeleted(true);
        stock.setAmount(0);
        stock.setCurrentPrice(BigDecimal.ZERO);
        orderRepository.findByStock(stock).forEach(order -> {
            orderRepository.delete(order);
            ArchivedOrder archivedOrder = archivedOrderRepository.findById(id)
                    .orElseGet(() -> modelMapper.map(order, ArchivedOrder.class));
            archivedOrder.setDateClosing(OffsetDateTime.now(ZoneId.systemDefault()));
            archivedOrderRepository.save(archivedOrder);
        });
        resourceRepository.deleteByStock(stock);
        stockIndexValueRepository.deleteByStock(stock);
        stock.getResources().clear();
        stockRepository.save(stock);
    }


    @Override
    @LogicBusinessMeasureTime
    @Transactional
    public void updateStockAmount(Long stockId, List<OwnerDTO> ownerDTOList) {
        Map<String, List<String>> errors = new HashMap<>();
        Stock stock = stockRepository.findById(stockId).orElseThrow(() -> new EntityNotFoundException("Stock not found"));
        ownerDTOList.forEach(ownerDTO -> {
            Optional<User> user = userRepository.findById(ownerDTO.getUser().getId());
            if (user.isEmpty()) {
                errors.put("owners[" + ownerDTOList.indexOf(ownerDTO) + ']', List.of("User not found"));
            } else if (user.get().getRole().equals(Role.ADMIN)) {
                errors.put("owners[" + ownerDTOList.indexOf(ownerDTO) + ']', List.of("User is an admin"));
            }
        });
        if (!errors.isEmpty()) {
            throw new InvalidInputDataException("Data validation", errors);
        }
        List<Resource> resources = ownerDTOList.stream().map(ownerDTO -> {
            Optional<User> user = userRepository.findById(ownerDTO.getUser().getId());
            Optional<Resource> resource = resourceRepository.findByUserAndStock(user.get(), stock);
            if (resource.isEmpty()) {
                return Resource.builder()
                        .stock(stock)
                        .user(user.get())
                        .amount(0)
                        .build();
            }
            return resource.get();
        }).collect(Collectors.toList());
        int currentStockAmount = resources.stream().mapToInt(Resource::getAmount).sum();
        int insertedStockAmount = ownerDTOList.stream().mapToInt(OwnerDTO::getAmount).sum();
        for (int i = 0; i < ownerDTOList.size(); i++) {
            resources.get(i).setAmount(ownerDTOList.get(i).getAmount());
        }
        stock.setAmount(insertedStockAmount - currentStockAmount + stock.getAmount());
        resources.addAll(stock.getResources());
        stock.setResources(resources);
        stockRepository.save(stock);
    }

    private Stock validateCreateStockDTO(CreateStockDTO stockDTO, String tag) {
        Map<String, List<String>> errors = new HashMap<>();
        Optional<Stock> stockInDBName = stockRepository.findByNameIgnoreCase(stockDTO.getName().trim());
        if (stockInDBName.isPresent() && !stockInDBName.get().getIsDeleted()) {
            throw new EntityExistsException("Stock with given name already exists.");
        }
        Optional<Stock> stockInDBAbbreviation = stockRepository.findByAbbreviationIgnoreCase(stockDTO.getAbbreviation().trim());
        if (stockInDBAbbreviation.isPresent() && !stockInDBAbbreviation.get().getIsDeleted()) {
            throw new EntityExistsException("Stock with given abbreviation already exists.");
        }
        Stock stock = stockInDBName.isPresent() && (stockInDBAbbreviation.isEmpty() ||
                stockInDBName.get().getId().equals(stockInDBAbbreviation.get().getId())) ?
                stockInDBName.get() : stockInDBAbbreviation
                .orElseGet(() -> modelMapper.map(stockDTO, Stock.class));
        stock.setName(stockDTO.getName().trim());
        stock.setAbbreviation(stockDTO.getAbbreviation().trim());
        stock.setIsDeleted(false);
        stock.setAmount(stockDTO.getAmount());
        stock.setCurrentPrice(stockDTO.getCurrentPrice());
        stock.setPriceChangeRatio(.0);
        stock.setTag(tagService.getTag(tag));
        List<Optional<User>> potentialUsers = stockDTO.getOwners().stream()
                .map(ownerDTO -> userRepository.findById(ownerDTO.getUser().getId()))
                .collect(Collectors.toList());
        List<OwnerDTO> filteredOwner = stockDTO.getOwners().stream()
                .filter(ownerDTO -> {
                    int index = stockDTO.getOwners().indexOf(ownerDTO);
                    Optional<User> user = potentialUsers.get(index);
                    if (user.isEmpty()) {
                        errors.put("owners[" + index + "]", List.of("User not found."));
                        return false;
                    } else if (user.get().getRole() == Role.ADMIN) {
                        errors.put("owners[" + index + "]", List.of("Given user is admin."));
                        return false;
                    } else if (!user.get().getTag().getName().toUpperCase().equals(tag.toUpperCase())) {
                        errors.put("owners[" + index + "]", List.of("Owner is tagged using another tag than creating stock."));
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());
        if (potentialUsers.size() == filteredOwner.size()) {
                stock.setResources(filteredOwner.stream().map(ownerDTO -> {
                    int index = stockDTO.getOwners().indexOf(ownerDTO);
                    return Resource.builder()
                            .stock(stock)
                            .amount(ownerDTO.getAmount())
                            .user(potentialUsers.get(index).get())
                            .build();
                }).collect(Collectors.toList()));
        }
        if (stockDTO.getAmount() != stockDTO.getOwners().stream().mapToInt(OwnerDTO::getAmount).sum()) {
            errors.put("amount",
                    List.of("Given amount of stocks must be equal sum of amounts specified in owners' list."));
        }
        if (!errors.isEmpty()) {
            throw new InvalidInputDataException("Data validation", errors);
        }
        return stock;
    }

}

