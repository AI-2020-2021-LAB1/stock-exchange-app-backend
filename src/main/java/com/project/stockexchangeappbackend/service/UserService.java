package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserService {

    void registerUser(RegistrationUserDTO registrationUserDTO);
    User findUserById(Long id);
    User findUserByEmail(String email);
    User changeUserPassword(Long id, String password);
    User changeUserDetails(Long id, String firstName, String lastName);
    Page<User> getUsers(Pageable pageable, Specification<User>  specification);

}
