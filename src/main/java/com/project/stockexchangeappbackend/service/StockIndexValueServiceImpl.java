package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.StockIndexValueDTO;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.StockIndexValue;
import com.project.stockexchangeappbackend.repository.StockIndexValueRepository;
import com.project.stockexchangeappbackend.repository.StockRepository;
import com.project.stockexchangeappbackend.util.StockIndexTimeProperties;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class StockIndexValueServiceImpl implements StockIndexValueService {

    private final StockRepository stockRepository;
    private final StockIndexValueRepository stockIndexValueRepository;
    private final StockIndexTimeProperties stockIndexTimeProperties;

    @Override
    @LogicBusinessMeasureTime
    @Transactional
    public void appendValue(StockIndexValue stockIndexValue) {
        stockIndexValueRepository.save(stockIndexValue);
        final int records = stockIndexTimeProperties.getMaxPriceHistoryPeriod()*3600/
                (stockIndexTimeProperties.getFixingPriceCycle()/1000);
        if (stockIndexValueRepository.count(getSpecificationById(stockIndexValue.getStock().getId())) > records) {
            stockIndexValueRepository.delete(
                    stockIndexValueRepository.findFirstByStockOrderByTimestampAsc(stockIndexValue.getStock()).get());
        }
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional(readOnly = true)
    public List<StockIndexValueDTO> getStockIndexValues(Long stockId, Specification<StockIndexValue> specification,
                                                     Integer interval) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));
        List<List<StockIndexValue>> results = new ArrayList<>();
        results.add(new ArrayList<>());

        List<StockIndexValue> stockIndexValues = stockIndexValueRepository.findAll(
                getSpecificationById(stock.getId()).and(specification), Sort.by("timestamp").descending());

        if (stockIndexValues.isEmpty()){
            return Collections.emptyList();
        }

        stockIndexValues.forEach(stockIndexValue -> {
            if (results.get(results.size()-1).isEmpty()) {
                results.get(results.size()-1).add(stockIndexValue);
            } else if (stockIndexValue.getTimestamp().isAfter(
                    results.get(results.size()-1).get(0).getTimestamp().minusMinutes(interval))) {
                results.get(results.size()-1).add(stockIndexValue);
            } else {
                results.add(new ArrayList<>());
                results.get(results.size()-1).add(stockIndexValue);
            }
        });
        Collections.reverse(results);
        return results.parallelStream()
                .map(StockIndexValueDTO::new)
                .collect(Collectors.toList());
    }

    private Specification<StockIndexValue> getSpecificationById(Long stockId) {
        return  (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.join("stock").get("id"), stockId);
    }

}
