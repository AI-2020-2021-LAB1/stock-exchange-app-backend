package com.project.stockexchangeappbackend.repositories;

import com.project.stockexchangeappbackend.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
