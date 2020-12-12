package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.SystemResourcesMonitor;
import com.project.stockexchangeappbackend.repository.specification.SystemResourceMonitorSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface SystemResourcesMonitorService {

    void addSystemResources();
    Page<SystemResourcesMonitor> getInfo(Pageable pageable, Specification<SystemResourcesMonitor> specification);

}
