package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.entity.Role;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.repository.UserRepository;
import com.project.stockexchangeappbackend.util.timemeasuring.LogicBusinessMeasureTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @LogicBusinessMeasureTime
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

    @Override
    @Transactional(readOnly = true)
    @LogicBusinessMeasureTime
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
    }

    @Override
    @Transactional(readOnly = true)
    @LogicBusinessMeasureTime
    public Page<User> getUsers(Pageable pageable, Specification<User> specification) {
        return userRepository.findAll(specification, pageable);
    }

}
