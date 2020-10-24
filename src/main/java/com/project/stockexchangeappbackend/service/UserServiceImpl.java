package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.entity.Role;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerUser(RegistrationUserDTO registrationUserDTO) {
        if (userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail()).isPresent()) {
            throw new EntityExistsException("User with given email already exists.");
        }
        userRepository.save(User.builder()
                .email(registrationUserDTO.getEmail().trim())
                .password(passwordEncoder.encode(registrationUserDTO.getPassword()))
                .firstName(registrationUserDTO.getFirstName().trim())
                .lastName(registrationUserDTO.getLastName().trim())
                .role(Role.USER)
                .money(BigDecimal.ZERO)
                .build());
       log.info("User " + registrationUserDTO.getEmail() + " was successfully registered.");
    }

}
