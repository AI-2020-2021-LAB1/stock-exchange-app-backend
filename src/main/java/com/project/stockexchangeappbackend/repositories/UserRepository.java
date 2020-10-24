package com.project.stockexchangeappbackend.repositories;

import com.project.stockexchangeappbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
