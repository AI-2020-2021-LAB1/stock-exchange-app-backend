package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.ChangePasswordDTO;
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
import org.springframework.security.access.AccessDeniedException;
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
    private final TagService tagService;

    private boolean validPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])" +
                "[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,}$");
    }

    @Override
    @Transactional
    @LogicBusinessMeasureTime
    public void registerUser(RegistrationUserDTO registrationUserDTO, String tag) {
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
                .tag(tagService.getTag(tag.trim()))
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
    public User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
    }

    @Override
    @LogicBusinessMeasureTime
    @Transactional
    public User changeUserPassword(Long id, ChangePasswordDTO changePasswordDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        if (!validPassword(changePasswordDTO.getNewPassword())) {
            throw new AccessDeniedException("New password is too weak.");
        }
        if (passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
            userRepository.save(user);
            return user;
        } else {
            throw new AccessDeniedException("Passwords do not match.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @LogicBusinessMeasureTime
    public Page<User> getUsers(Pageable pageable, Specification<User> specification) {
        return userRepository.findAll(specification, pageable);
    }

}
