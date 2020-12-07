package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.dto.ChangePasswordDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.security.access.AccessDeniedException;
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
import static com.project.stockexchangeappbackend.service.TagServiceImplTest.getTagsList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

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

    @BeforeEach
    void setup() {
        setUsersList();
    }

    @Test
    @DisplayName("Signing up new user")
    void shouldRegisterUser() {
        User user = getUsersList().get(0);
        RegistrationUserDTO registrationUserDTO =
                new RegistrationUserDTO(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
        Tag tag = getTagsList().get(0);

        when(userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registrationUserDTO.getPassword())).thenReturn("encodedPassword");
        when(tagService.getTag(tag.getName())).thenReturn(Optional.of(tag));
        assertAll(() -> userService.registerUser(registrationUserDTO, tag.getName()));
    }

    @Test
    @DisplayName("Signing up new user when user already exist")
    void shouldThrowEntityExistsExceptionWhenRegisterUser() {
        User user = getUsersList().get(0);
        RegistrationUserDTO registrationUserDTO =
                new RegistrationUserDTO(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());

        Tag tag = getTagsList().get(0);
        when(userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail())).thenReturn(Optional.of(user));
        assertThrows(EntityExistsException.class, () -> userService.registerUser(registrationUserDTO, tag.getName()));
    }

    @Test
    @DisplayName("Signing up new user when tag not found")
    void shouldThrowInvalidInputDataExceptionWhenRegisteringUser() {
        User user = getUsersList().get(0);
        RegistrationUserDTO registrationUserDTO =
                new RegistrationUserDTO(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
        String tag = "non";

        when(userRepository.findByEmailIgnoreCase(registrationUserDTO.getEmail())).thenReturn(Optional.empty());
        when(tagService.getTag(tag)).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> userService.registerUser(registrationUserDTO, tag));
    }

    @Test
    @DisplayName("Getting user by id")
    void shouldReturnUserById() {
        User user = getUsersList().get(0);
        Long id = user.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertUser(userService.findUserById(id), user);
    }

    @Test
    @DisplayName("Getting user by id when user not found")
    void shouldThrowEntityExistsExceptionWhenGettingUserById() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.findUserById(id));
    }

    @Test
    @DisplayName("Getting user by email")
    void shouldReturnUserByEmail() {
        User user = getUsersList().get(0);
        String email = user.getEmail();
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        assertUser(userService.findUserByEmail(email), user);
    }

    @Test
    @DisplayName("Getting user by email when user not found")
    void shouldThrowEntityExistsExceptionWhenGettingUserByEmail() {
        String email = "test@test.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.findUserByEmail(email));
    }

    @Test
    @DisplayName("Changing user's password")
    void shouldChangePassword() {
        User user = getUsersList().get(0);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO("oldPassword", "newPassword");
        Principal principal = user::getEmail;

        when(userRepository.findByEmailIgnoreCase(principal.getName())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())).thenReturn(Boolean.TRUE);
        assertAll(() -> userService.changeUserPassword(changePasswordDTO, principal));
    }

    @Test
    @DisplayName("Changing user's password when current password wrong")
    void shouldThrowAccessDeniedExceptionWhenChangingPassword() {
        User user = getUsersList().get(0);
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO("oldPassword", "newPassword");
        Principal principal = user::getEmail;

        when(userRepository.findByEmailIgnoreCase(principal.getName())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())).thenReturn(Boolean.FALSE);
        assertThrows(AccessDeniedException.class, () -> userService.changeUserPassword(changePasswordDTO, principal));
    }

    @Test
    @DisplayName("Changing user's password when user not found")
    void shouldThrowEntityNotFoundExceptionWhenChangingPassword() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO("oldPassword", "newPassword");
        Principal principal = () -> "username";

        when(userRepository.findByEmailIgnoreCase(principal.getName())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.changeUserPassword(changePasswordDTO, principal));
    }

    @Test
    @DisplayName("Updating user's details")
    void shouldUpdateUsersNames() {
        EditUserNameDTO editUserNameDTO = new EditUserNameDTO("John", "Kowal");
        User user = getUsersList().get(0);
        Principal principal = user::getEmail;

        when(userRepository.findByEmailIgnoreCase(principal.getName())).thenReturn(Optional.of(user));
        assertAll(() -> userService.changeUserDetails(editUserNameDTO, principal));
    }

    @Test
    @DisplayName("Updating user's details when user not found")
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUsersNames() {
        EditUserNameDTO editUserNameDTO = new EditUserNameDTO("John", "Kowal");
        Principal principal = () -> "test@test";

        when(userRepository.findByEmailIgnoreCase(principal.getName())).thenReturn(Optional.empty());
        assertThrows(InvalidInputDataException.class, () -> userService.changeUserDetails(editUserNameDTO, principal));
    }

    @Test
    @DisplayName("Paging and filtering users")
    void shouldPageAndFilterUsers() {
        List<User> users = getUsersList();
        Pageable pageable = PageRequest.of(0, 20);
        Specification<User> specification = (root, criteriaQuery, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("lastName"), users.get(0).getLastName());

        when(userRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(users, pageable, users.size()));
        Page<User> output = userService.getUsers(pageable, specification);
        assertEquals(users.size(), output.getNumberOfElements());
        for (int i = 0; i < users.size(); i++) {
            assertUser(output.getContent().get(i), users.get(i));
        }
    }

    @Test
    @DisplayName("Updating user's details as admin")
    void shouldUpdateUser() {
        User user = getUsersList().get(1);
        Long id = user.getId();
        EditUserDetailsDTO editUserDetailsDTO =
                new EditUserDetailsDTO("John", "Kowal", Role.ADMIN, true);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertAll(() -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    @DisplayName("Updating user's details as admin when removing last admin")
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndRemovingLastAdmin() {
        User user = getUsersList().get(1);
        Long id = user.getId();
        EditUserDetailsDTO editUserDetailsDTO =
                new EditUserDetailsDTO("John", "Kowal", Role.USER, true);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    @DisplayName("Updating user's details as admin when blocking admin")
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndBlockingAdmin() {
        User user = getUsersList().get(1);
        Long id = user.getId();
        EditUserDetailsDTO editUserDetailsDTO =
                new EditUserDetailsDTO("John", "Kowal", Role.ADMIN, false);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    @DisplayName("Updating user's details as admin when blocking admin")
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndBlockingAdmin2() {
        User user = getUsersList().get(0);
        Long id = user.getId();
        EditUserDetailsDTO editUserDetailsDTO =
                new EditUserDetailsDTO("John", "Kowal", Role.ADMIN, false);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(allOrdersRepository.countByUser(user)).thenReturn(0L);
        when(resourceRepository.countByUser(user)).thenReturn(0L);
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    @DisplayName("Updating user's details as admin when creating admin who possess orders")
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndChangingUserToAdminPossessingOrder() {
        User user = getUsersList().get(0);
        Long id = user.getId();
        EditUserDetailsDTO editUserDetailsDTO =
                new EditUserDetailsDTO("John", "Kowal", Role.ADMIN, true);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(allOrdersRepository.countByUser(user)).thenReturn(1L);
        when(resourceRepository.countByUser(user)).thenReturn(0L);
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    @DisplayName("Updating user's details as admin when creating admin who possess stocks")
    void shouldThrowInvalidInputDataExceptionWhenUpdatingUserAndChangingUserToAdminPossessingStocks() {
        User user = getUsersList().get(0);
        Long id = user.getId();
        EditUserDetailsDTO editUserDetailsDTO = new EditUserDetailsDTO("John", "Kowal",
                Role.ADMIN, true);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(allOrdersRepository.countByUser(user)).thenReturn(0L);
        when(resourceRepository.countByUser(user)).thenReturn(1L);
        assertThrows(InvalidInputDataException.class, () -> userService.updateUser(id, editUserDetailsDTO));
    }

    @Test
    @DisplayName("Updating user's details as admin when user not found")
    void shouldThrowEntityNotFoundWhenUpdatingNonExistingUser() {
        Long id = 1L;
        EditUserDetailsDTO editUserDetailsDTO =
                new EditUserDetailsDTO("John", "Kowal", Role.ADMIN, true);

        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(id, editUserDetailsDTO));
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


    private static List<User> users;

    public static List<User> getUsersList() {
        if (users == null) {
            setUsersList();
        }
        return users;
    }

    private static void setUsersList() {
        var tags = getTagsList();
        users = Arrays.asList(
                User.builder()
                        .id(1L).email("user@test").firstName("John").lastName("Kowal").password("password")
                        .money(BigDecimal.TEN).role(Role.USER).isActive(true).tag(tags.get(0))
                        .userStocks(new ArrayList<>())
                        .build(),
                User.builder()
                        .id(2L).email("user2@test").firstName("Jane").lastName("Kowal").password("password")
                        .money(BigDecimal.ZERO).role(Role.ADMIN).isActive(true).tag(tags.get(0))
                        .userStocks(new ArrayList<>())
                        .build(),
                User.builder()
                        .id(3L).email("user3@test").firstName("John").lastName("Kowal").password("password")
                        .money(BigDecimal.TEN).role(Role.USER).isActive(true).tag(tags.get(0))
                        .userStocks(new ArrayList<>())
                        .build());
    }

}
