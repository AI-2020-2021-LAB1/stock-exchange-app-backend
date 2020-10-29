package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Resource;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    @DBQueryMeasureTime
    <S extends Resource> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<Resource> findById(Long id);

}
