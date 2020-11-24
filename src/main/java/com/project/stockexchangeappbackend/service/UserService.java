package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.ChangePasswordDTO;
import com.project.stockexchangeappbackend.dto.EditUserDetailsDTO;
import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.security.Principal;

public interface UserService {

    void registerUser(RegistrationUserDTO registrationUserDTO, String tag);
    User findUserById(Long id);
    User findUserByEmail(String email);
    User changeUserDetails(Long id, String firstName, String lastName);
    void changeUserPassword(ChangePasswordDTO changePasswordDTO, Principal principal);
    Page<User> getUsers(Pageable pageable, Specification<User>  specification);
    void updateUser(Long id, EditUserDetailsDTO editUserDetailsDTO);

}
