package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Tag;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @DBQueryMeasureTime
    Optional<Tag> findByNameIgnoreCase(String name);

    @Override
    @DBQueryMeasureTime
    <S extends Tag> S save(S s);
}
