package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.SystemResourcesMonitor;
import com.project.stockexchangeappbackend.repository.SystemResourcesMonitorRepository;
import com.project.stockexchangeappbackend.util.StockIndexTimeProperties;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import oshi.hardware.GlobalMemory;

import java.lang.management.ManagementFactory;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@AllArgsConstructor
public class SystemResourcesMonitorServiceImpl implements SystemResourcesMonitorService{

    private final SystemResourcesMonitorRepository systemResourcesMonitorRepository;
    private final GlobalMemory globalMemory;
    private final StockIndexTimeProperties stockIndexTimeProperties;

    @Override
    @Transactional
    public void addSystemResources() {
        long memoryUsed = globalMemory.getTotal() - globalMemory.getAvailable();
        systemResourcesMonitorRepository.save(SystemResourcesMonitor.builder()
                .timestamp(OffsetDateTime.now(ZoneId.systemDefault()))
                .cpuUsage(ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage())
                .memoryUsage((double)memoryUsed/globalMemory.getTotal())
                .memoryUsed(memoryUsed)
                .build());
        int records = stockIndexTimeProperties.getSystemResourcesMonitorHistory()*3600000/
                stockIndexTimeProperties.getSystemResourcesMonitorInterval();
        if (systemResourcesMonitorRepository.count() > records) {
            systemResourcesMonitorRepository.delete(systemResourcesMonitorRepository.findFirstByOrderByTimestampAsc());
        }
    }

    @Override
    @Transactional(readOnly = true)
    @LogicBusinessMeasureTime
    public List<SystemResourcesMonitor> getInfo(Specification<SystemResourcesMonitor> specification) {
        return systemResourcesMonitorRepository.findAll(specification, Sort.by("timestamp").ascending());
    }

}
