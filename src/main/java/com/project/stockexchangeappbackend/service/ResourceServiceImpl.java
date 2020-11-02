package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Resource;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.repository.ResourceRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Join;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService{

    private final ResourceRepository resourceRepository;

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public Page<Resource> getOwnedResources(Pageable pageable, Specification<Resource> resourceSpecification) {
        String principal = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Specification<Resource> userIsPrincipal = (root, criteriaQuery, criteriaBuilder) -> {
            Join<Resource, User> owner = root.join("user");
            return criteriaBuilder.equal(owner.get("email"), principal);
        };
        return resourceRepository.findAll(Specification.where(userIsPrincipal).and(resourceSpecification), pageable);
    }

}
