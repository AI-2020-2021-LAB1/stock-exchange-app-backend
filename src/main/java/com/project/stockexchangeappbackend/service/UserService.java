package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.entity.User;

public interface UserService {

    void registerUser(RegistrationUserDTO registrationUserDTO);
    User findUserById(Long id);

}
