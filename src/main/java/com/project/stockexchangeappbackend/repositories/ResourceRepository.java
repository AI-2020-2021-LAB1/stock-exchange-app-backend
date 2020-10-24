package com.project.stockexchangeappbackend.repositories;

import com.project.stockexchangeappbackend.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findAll();
}
