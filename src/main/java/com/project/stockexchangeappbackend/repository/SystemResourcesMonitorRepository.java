package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.SystemResourcesMonitor;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemResourcesMonitorRepository extends JpaRepository<SystemResourcesMonitor, Long>,
                                                          JpaSpecificationExecutor<SystemResourcesMonitor> {

    SystemResourcesMonitor findFirstByOrderByTimestampAsc();

    @Override
    @DBQueryMeasureTime
    List<SystemResourcesMonitor> findAll(@Nullable Specification<SystemResourcesMonitor> specification, Sort sort);

}
