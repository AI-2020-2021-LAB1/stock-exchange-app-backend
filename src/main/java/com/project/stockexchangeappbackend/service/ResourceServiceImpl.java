package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.ResourceDTO;
import com.project.stockexchangeappbackend.entity.Order;
import com.project.stockexchangeappbackend.entity.OrderType;
import com.project.stockexchangeappbackend.entity.Resource;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.repository.OrderRepository;
import com.project.stockexchangeappbackend.repository.ResourceRepository;
import com.project.stockexchangeappbackend.repository.UserRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Join;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Page<ResourceDTO> getOwnedResources(Pageable pageable, Specification<Resource> resourceSpecification) {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return findResources(pageable, resourceSpecification, principal);
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Page<ResourceDTO> getUsersResources(Pageable pageable, Specification<Resource> specification, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return findResources(pageable, specification, user.getEmail());
    }

    private Page<ResourceDTO> findResources(Pageable pageable, Specification<Resource> specification, String username) {
        Specification<Resource> userIsPrincipal = (root, criteriaQuery, criteriaBuilder) -> {
            Join<Resource, User> owner = root.join("user");
            return criteriaBuilder.equal(owner.get("email"), username);
        };
        return resourceRepository.findAll(Specification.where(userIsPrincipal).and(specification), pageable)
                .map(resource -> {
                    ResourceDTO resourceDTO = modelMapper.map(resource, ResourceDTO.class);
                    int sellingAmountOfStock =
                            orderRepository.findByStockAndUserAndOrderTypeAndDateExpirationIsAfterAndDateClosingIsNull(
                                    resource.getStock(), resource.getUser(), OrderType.SELLING_ORDER,
                                    OffsetDateTime.now(ZoneId.systemDefault()))
                                    .stream()
                                    .mapToInt(Order::getRemainingAmount)
                                    .sum();
                    resourceDTO.setAmountAvailableForSale(resourceDTO.getAmount() - sellingAmountOfStock);
                    return resourceDTO;
                });
    }

}
