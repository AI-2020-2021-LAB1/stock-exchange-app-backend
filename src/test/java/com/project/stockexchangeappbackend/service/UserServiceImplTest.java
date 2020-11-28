package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.EditUserDetailsDTO;
import com.project.stockexchangeappbackend.dto.EditUserNameDTO;
import com.project.stockexchangeappbackend.dto.RegistrationUserDTO;
import com.project.stockexchangeappbackend.entity.Role;
import com.project.stockexchangeappbackend.entity.Tag;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.exception.InvalidInputDataException;
import com.project.stockexchangeappbackend.repository.AllOrdersRepository;
import com.project.stockexchangeappbackend.repository.ResourceRepository;
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
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.project.stockexchangeappbackend.service.TagServiceImplTest.assertTag;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    AllOrdersRepository allOrdersRepository;

    @Mock
    ResourceRepository resourceRepository;

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
        Tag tag = new Tag(1L, "DEFAULT");
        User user = createCustomUser(id, "test@test.com", "John", "Nowak", BigDecimal.ZERO, tag);
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
    void shouldReturnUserByEmail() {
        String email = "test@test.com";
        Tag tag = new Tag(1L, "DEFAULT");
        User user = createCustomUser(1L, email, "John", "Nowak", BigDecimal.ZERO, Role.USER, tag);
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        assertUser(userService.findUserByEmail(email), user);
    }

    @Test
    void shouldThrowEntityExistsExceptionWhenGettingUserByEmail() {
        String email = "test@test.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.findUserByEmail(email));
    }

    @Test
    void shouldPageAndFilterUsers() {
        Tag tag = new Tag(1L, "DEFAULT");
        List<User> users = Arrays.asList(
            createCustomUser(1L, "test1@test.pl", "John", "Kowal", BigDecimal.ZERO, tag),
            createCustomUser(2L, "test@test.pl", "Jane", "Kowal", BigDecimal.TEN, tag)
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

    @Test
    void shouldUpdateUser() {
        Long id = 1L;
        EditUserDetailsDTO editUserDetailsDTO = createEditUserDetailsDTO("John", "Kowal",
                Role.ADMIN, true);
        Tag tag = new Tag(1L, "default");
        User user = createCustomUser(id, "test@test", "John", "Kowal", BigDecimal.ZERO,
                Role.ADMIN, true, tag);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertAll(() -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndRemovingLastAdmin() {
        Long id = 1L;
        EditUserDetailsDTO editUserDetailsDTO = createEditUserDetailsDTO("John", "Kowal",
                Role.USER, true);
        Tag tag = new Tag(1L, "default");
        User user = createCustomUser(id, "test@test", "John", "Kowal", BigDecimal.ZERO,
                Role.ADMIN, true, tag);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndBlockingAdmin() {
        Long id = 1L;
        EditUserDetailsDTO editUserDetailsDTO = createEditUserDetailsDTO("John", "Kowal",
                Role.ADMIN, false);
        Tag tag = new Tag(1L, "default");
        User user = createCustomUser(id, "test@test", "John", "Kowal", BigDecimal.ZERO,
                Role.ADMIN, true, tag);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndBlockingAdmin2() {
        Long id = 1L;
        EditUserDetailsDTO editUserDetailsDTO = createEditUserDetailsDTO("John", "Kowal",
                Role.ADMIN, false);
        Tag tag = new Tag(1L, "default");
        User user = createCustomUser(id, "test@test", "John", "Kowal", BigDecimal.ZERO,
                Role.USER, true, tag);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(allOrdersRepository.countByUser(user)).thenReturn(0L);
        when(resourceRepository.countByUser(user)).thenReturn(0L);
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndChangingUserToAdminPossessingOrder() {
        Long id = 1L;
        EditUserDetailsDTO editUserDetailsDTO = createEditUserDetailsDTO("John", "Kowal",
                Role.ADMIN, true);
        Tag tag = new Tag(1L, "default");
        User user = createCustomUser(id, "test@test", "John", "Kowal", BigDecimal.ZERO,
                Role.USER, true, tag);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(allOrdersRepository.countByUser(user)).thenReturn(1L);
        when(resourceRepository.countByUser(user)).thenReturn(0L);
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndChangingUserToAdminPossessingStocks() {
        Long id = 1L;
        EditUserDetailsDTO editUserDetailsDTO = createEditUserDetailsDTO("John", "Kowal",
                Role.ADMIN, true);
        Tag tag = new Tag(1L, "default");
        User user = createCustomUser(id, "test@test", "John", "Kowal", BigDecimal.ZERO,
                Role.USER, true, tag);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(allOrdersRepository.countByUser(user)).thenReturn(0L);
        when(resourceRepository.countByUser(user)).thenReturn(1L);
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    void shouldThrowEntityNotFoundWhenUpdatingNonExistingUser() {
        Long id = 1L;
        EditUserDetailsDTO editUserDetailsDTO = createEditUserDetailsDTO("John", "Kowal",
                Role.ADMIN, true);
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    void shouldUpdateUsersNames() {
        EditUserNameDTO editUserNameDTO = new EditUserNameDTO("John", "Kowal");
        Principal principal = () -> "test@test";
        Tag tag = new Tag(1L, "default");
        User user = createCustomUser(1L, principal.getName(), "Jan", "jan", BigDecimal.ZERO, Role.USER, tag);
        when(userRepository.findByEmailIgnoreCase(principal.getName())).thenReturn(Optional.of(user));
        assertAll(() -> userService.changeUserDetails(editUserNameDTO, principal));
    }

    @Test
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUsersNames() {
        EditUserNameDTO editUserNameDTO = new EditUserNameDTO("John", "Kowal");
        Principal principal = () -> "test@test";
        when(userRepository.findByEmailIgnoreCase(principal.getName())).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> userService.changeUserDetails(editUserNameDTO, principal));
    }

    public static void assertUser(User output, User expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getEmail(), output.getEmail()),
                () -> assertEquals(expected.getFirstName(), output.getFirstName()),
                () -> assertEquals(expected.getLastName(), output.getLastName()),
                () -> assertEquals(expected.getMoney(), output.getMoney()),
                () -> assertEquals(expected.getRole(), output.getRole()),
                () -> assertTag(expected.getTag(), output.getTag()),
                () -> assertEquals(expected.getIsActive(), output.getIsActive()));
    }

    public static User createCustomUser (Long id, String email, String firstName, String lastName, BigDecimal money) {
        return User.builder()
                .id(id).email(email)
                .firstName(firstName).lastName(lastName)
                .money(money)
                .orders(new ArrayList<>())
                .userStocks(new ArrayList<>())
                .isActive(true)
                .build();
    }

    public static User createCustomUser (Long id, String email, String firstName, String lastName, BigDecimal money,
                                         Tag tag) {
        return User.builder()
                .id(id).email(email)
                .firstName(firstName).lastName(lastName)
                .money(money)
                .orders(new ArrayList<>())
                .userStocks(new ArrayList<>())
                .tag(tag)
                .isActive(true)
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

    public static User createCustomUser (Long id, String email, String firstName, String lastName, BigDecimal money,
                                         Role role, Boolean isActive, Tag tag) {
        return User.builder()
                .id(id).email(email)
                .firstName(firstName).lastName(lastName)
                .money(money)
                .role(role)
                .tag(tag)
                .isActive(isActive)
                .build();
    }

    public static RegistrationUserDTO createRegistrationUserDTO (String email, String password,
                                                                  String firstName, String lastName) {
        return RegistrationUserDTO.builder()
                .email(email).password(password)
                .firstName(firstName).lastName(lastName).build();
    }

    public static EditUserDetailsDTO createEditUserDetailsDTO(String firstName, String lastName, Role role, Boolean active) {
        return EditUserDetailsDTO.builder()
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .isActive(active)
                .build();
    }

}
