package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.ResourceDTO;
import com.project.stockexchangeappbackend.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ResourceService {

    Page<ResourceDTO> getOwnedResources(Pageable pageable, Specification<Resource> specification);
    Page<ResourceDTO> getUsersResources(Pageable pageable, Specification<Resource> specification, Long userId);
}
