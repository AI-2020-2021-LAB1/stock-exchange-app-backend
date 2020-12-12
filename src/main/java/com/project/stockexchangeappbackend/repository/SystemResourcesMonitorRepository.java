package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.SystemResourcesMonitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemResourcesMonitorRepository extends JpaRepository<SystemResourcesMonitor, Long>,
                                                          JpaSpecificationExecutor<SystemResourcesMonitor> {

    SystemResourcesMonitor findFirstByOrderByTimestampAsc();

}
