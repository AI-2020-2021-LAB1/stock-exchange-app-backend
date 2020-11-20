package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.entity.Role;
import com.project.stockexchangeappbackend.entity.Tag;
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
import java.util.ArrayList;
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

    @Mock
    TagService tagService;

    @Test
    void shouldRegisterUser() {
        RegistrationUserDTO registrationUserDTO =
                createRegistrationUserDTO("test@test.com", "secret" ,"Jan", "Kowalski");
        Tag tag = new Tag(1L, "DEFAULT");
        when(userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail().trim())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationUserDTO.getPassword())).thenReturn("encodedPassword");
        when(tagService.getTag(tag.getName())).thenReturn(tag);
        assertAll(() -> userService.registerUser(registrationUserDTO, tag.getName()));
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenRegisterUser() {
        RegistrationUserDTO registrationUserDTO =
                createRegistrationUserDTO("test@test.com", "secret" ,"Jan", "Kowalski");
        Tag tag = new Tag(1L, "DEFAULT");
        when(userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail().trim()))
                .thenReturn(Optional.of(User.builder().email(registrationUserDTO.getEmail()).build()));
        assertThrows(EntityExistsException.class, () -> userService.registerUser(registrationUserDTO, tag.getName()));
    }

    @Test
    void shouldReturnUserById() {
        Long id = 1L;
        User user = createCustomUser(id, "test@test.com", "John", "Nowak", BigDecimal.ZERO,
                "DEFAULT");
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
                createCustomUser(1L, "test1@test.pl", "John", "Kowal", BigDecimal.ZERO, "DEFAULT"),
                createCustomUser(2L, "test@test.pl", "Jane", "Kowal", BigDecimal.TEN, "DEFAULT")
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
                () -> assertEquals(expected.getMoney(), output.getMoney()),
                () -> assertEquals(expected.getRole(), output.getRole()),
                () -> assertEquals(expected.getTag(), output.getTag()));
    }

    public static User createCustomUser (Long id, String email, String firstName, String lastName, BigDecimal money) {
        return User.builder()
                .id(id).email(email)
                .firstName(firstName).lastName(lastName)
                .money(money)
                .orders(new ArrayList<>())
                .userStocks(new ArrayList<>())
                .build();
    }

    public static User createCustomUser (Long id, String email, String firstName, String lastName, BigDecimal money,
                                         String tag) {
        return User.builder()
                .id(id).email(email)
                .firstName(firstName).lastName(lastName)
                .money(money)
                .orders(new ArrayList<>())
                .userStocks(new ArrayList<>())
                .tag(new Tag(1L, tag))
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

    public static User createCustomUser (Long id, String email, String firstName, String lastName, BigDecimal money,
                                         Role role, Tag tag) {
        return User.builder()
                .id(id).email(email)
                .firstName(firstName).lastName(lastName)
                .money(money)
                .role(role)
                .tag(tag)
                .build();
    }

    public static RegistrationUserDTO createRegistrationUserDTO (String email, String password,
                                                                  String firstName, String lastName) {
        return RegistrationUserDTO.builder()
                .email(email).password(password)
                .firstName(firstName).lastName(lastName).build();
    }

}