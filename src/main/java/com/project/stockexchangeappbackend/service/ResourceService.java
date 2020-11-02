package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ResourceService {

    Page<Resource> getOwnedResources(Pageable pageable, Specification<Resource> specification);

}
