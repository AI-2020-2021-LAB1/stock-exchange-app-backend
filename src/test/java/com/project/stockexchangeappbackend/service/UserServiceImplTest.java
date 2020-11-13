package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.entity.Role;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
        RegistrationUserDTO registrationUserDTO =
                createRegistrationUserDTO("test@test.com", "secret" ,"Jan", "Kowalski");
        when(userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail().trim())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationUserDTO.getPassword())).thenReturn("encodedPassword");
        assertAll(() -> userService.registerUser(registrationUserDTO));
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenRegisterUser() {
        RegistrationUserDTO registrationUserDTO =
                createRegistrationUserDTO("test@test.com", "secret" ,"Jan", "Kowalski");
        when(userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail().trim()))
                .thenReturn(Optional.of(User.builder().email(registrationUserDTO.getEmail()).build()));
        assertThrows(EntityExistsException.class, () -> userService.registerUser(registrationUserDTO));
    }

    @Test
    void shouldReturnUserById() {
        Long id = 1L;
        User user = createCustomUser(id, "test@test.com", "John", "Nowak", BigDecimal.ZERO);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertUser(userService.findUserById(id), user);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenGettingUserById() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.findUserById(id));
    }

    @Test
    void shouldPageAndFilterUsers() {
        List<User> users = Arrays.asList(
                createCustomUser(1L, "test1@test.pl", "John", "Kowal", BigDecimal.ZERO),
                createCustomUser(2L, "test@test.pl", "Jane", "Kowal", BigDecimal.TEN)
        );
        Pageable pageable = PageRequest.of(0, 20);
        Specification<User> specification = (Specification<User>) (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("lastName"), "Kowal");
        when(userRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(users, pageable, users.size()));
        Page<User> output = userService.getUsers(pageable, specification);
        assertEquals(users.size(), output.getNumberOfElements());
        for (int i = 0; i < users.size(); i++) {
            assertUser(output.getContent().get(i), users.get(i));
        }
    }

    public static void assertUser(User output, User expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getEmail(), output.getEmail()),
                () -> assertEquals(expected.getFirstName(), output.getFirstName()),
                () -> assertEquals(expected.getLastName(), output.getLastName()),
                () -> assertEquals(expected.getMoney(), output.getMoney()));
    }

    public static User createCustomUser (Long id, String email, String firstName, String lastName, BigDecimal money) {
        return User.builder()
                .id(id).email(email)
                .firstName(firstName).lastName(lastName)
                .money(money)
                .build();
    }

    public static User createCustomUser (Long id, String email, String firstName, String lastName, BigDecimal money,
                                         Role role) {
        return User.builder()
                .id(id).email(email)
                .firstName(firstName).lastName(lastName)
                .money(money)
                .role(role)
                .build();
    }

    public static RegistrationUserDTO createRegistrationUserDTO (String email, String password,
                                                                  String firstName, String lastName) {
        return RegistrationUserDTO.builder()
                .email(email).password(password)
                .firstName(firstName).lastName(lastName).build();
    }

}