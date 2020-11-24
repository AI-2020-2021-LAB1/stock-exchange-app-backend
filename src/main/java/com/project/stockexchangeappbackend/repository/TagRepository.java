package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.Tag;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {

    @DBQueryMeasureTime
    Optional<Tag> findByNameIgnoreCase(String name);

    @Override
    @DBQueryMeasureTime
    <S extends Tag> S save(S s);

    @Override
    @DBQueryMeasureTime
    Page<Tag> findAll(@Nullable Specification<Tag> specification, Pageable pageable);

    @Override
    @DBQueryMeasureTime
    void delete(Tag tag);

}
