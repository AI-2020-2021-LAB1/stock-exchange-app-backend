package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityExistsException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    void shouldRegisterUser() {
        RegistrationUserDTO registrationUserDTO = RegistrationUserDTO.builder()
                .email("test@test.com").password("secret")
                .firstName("Jan").lastName("Kowalski").build();
        when(userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail().trim())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationUserDTO.getPassword())).thenReturn("encodedPassword");
        assertAll(() -> userService.registerUser(registrationUserDTO));
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenRegisterUser() {
        RegistrationUserDTO registrationUserDTO = RegistrationUserDTO.builder()
                .email("test@test.com").password("secret")
                .firstName("Jan").lastName("Kowalski").build();
        when(userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail().trim()))
                .thenReturn(Optional.of(User.builder().email(registrationUserDTO.getEmail()).build()));
        assertThrows(EntityExistsException.class, () -> userService.registerUser(registrationUserDTO));
    }

}