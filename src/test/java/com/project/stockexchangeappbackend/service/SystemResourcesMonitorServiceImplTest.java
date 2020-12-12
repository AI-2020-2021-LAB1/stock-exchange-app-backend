package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.SystemResourcesMonitor;
import com.project.stockexchangeappbackend.repository.SystemResourcesMonitorRepository;
import com.project.stockexchangeappbackend.util.StockIndexTimeProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import oshi.hardware.GlobalMemory;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemResourcesMonitorServiceImplTest {

    @InjectMocks
    SystemResourcesMonitorServiceImpl systemResourcesMonitorService;

    @Mock
    SystemResourcesMonitorRepository systemResourcesMonitorRepository;

    @Mock
    GlobalMemory globalMemory;

    @Mock
    StockIndexTimeProperties stockIndexTimeProperties;

    @Test
    @DisplayName("Saving system resource info")
    void shouldAddNewSystemResourcesMonitor() {
        when(globalMemory.getTotal()).thenReturn(1024L);
        when(globalMemory.getAvailable()).thenReturn(128L);
        when(systemResourcesMonitorRepository.count()).thenReturn(10L);
        when(stockIndexTimeProperties.getSystemResourcesMonitorHistory()).thenReturn(1);
        when(stockIndexTimeProperties.getSystemResourcesMonitorInterval()).thenReturn(500);
        assertAll(() -> systemResourcesMonitorService.addSystemResources());
    }

    @Test
    @DisplayName("Saving system resource info when max records exceeded")
    void shouldAddNewSystemResourcesMonitorWhenMaxRecordsExceeded() {
        when(globalMemory.getTotal()).thenReturn(1024L);
        when(globalMemory.getAvailable()).thenReturn(128L);
        when(systemResourcesMonitorRepository.count()).thenReturn(7300L);
        when(stockIndexTimeProperties.getSystemResourcesMonitorHistory()).thenReturn(1);
        when(stockIndexTimeProperties.getSystemResourcesMonitorInterval()).thenReturn(500);
        when(systemResourcesMonitorRepository.findFirstByOrderByTimestampAsc())
                .thenReturn(SystemResourcesMonitor.builder()
                        .timestamp(OffsetDateTime.now())
                        .cpuUsage(1.2)
                        .memoryUsed(255L)
                        .memoryUsage(25.0)
                        .build());
        assertAll(() -> systemResourcesMonitorService.addSystemResources());
    }

    @Test
    @DisplayName("Paging and filtering system resources")
    void shouldPageAndFilterSystemResources() {
        Pageable pageable = PageRequest.of(0, 20);
        Specification<SystemResourcesMonitor> specification = (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("datetime<"), OffsetDateTime.now());
        when(systemResourcesMonitorRepository.findAll(specification, pageable))
                .thenReturn(new PageImpl<>(
                        List.of(SystemResourcesMonitor.builder()
                        .timestamp(OffsetDateTime.now())
                        .cpuUsage(1.2)
                        .memoryUsed(255L)
                        .memoryUsage(25.0)
                        .build()), pageable, 1L));
        assertAll(() -> systemResourcesMonitorService.getInfo(pageable, specification));
    }

}