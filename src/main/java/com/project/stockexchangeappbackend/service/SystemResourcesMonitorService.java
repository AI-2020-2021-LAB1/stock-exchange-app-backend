package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.SystemResourcesMonitor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface SystemResourcesMonitorService {

    void addSystemResources();
    List<SystemResourcesMonitor> getInfo(Specification<SystemResourcesMonitor> specification);

}
