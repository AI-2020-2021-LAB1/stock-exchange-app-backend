package com.project.stockexchangeappbackend.repository;

import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.util.timemeasuring.DBQueryMeasureTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @DBQueryMeasureTime
    Optional<User> findByEmailIgnoreCase(String email);

    @Override
    @DBQueryMeasureTime
    <S extends User> S save(S s);

    @Override
    @DBQueryMeasureTime
    Optional<User> findById(Long id);

}
