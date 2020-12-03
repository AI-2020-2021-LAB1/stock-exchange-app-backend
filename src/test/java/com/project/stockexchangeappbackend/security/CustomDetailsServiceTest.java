package com.project.stockexchangeappbackend.security;

import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static com.project.stockexchangeappbackend.service.UserServiceImplTest.getUsersList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomDetailsServiceTest {

    @InjectMocks
    CustomDetailsService customDetailsService;

    @Mock
    UserRepository userRepository;

    @Test
    @DisplayName("Searching logging in user")
    void shouldFindLoggingInUser() {
        User user = getUsersList().get(0);
        String username = user.getEmail();
        when(userRepository.findByEmailIgnoreCaseAndIsActiveTrue(username)).thenReturn(Optional.of(user));
        var dbUser = customDetailsService.loadUserByUsername(username);
        assertEquals(user.getEmail(), dbUser.getUsername());
        assertEquals(user.getPassword(), dbUser.getPassword());
        assertEquals(1, dbUser.getAuthorities().size());
        assertEquals(Boolean.TRUE, dbUser.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_"+user.getRole())));
    }

    @Test
    @DisplayName("Searching logging in user when user not found")
    void shouldThrowUsernameNotFoundExceptionWhenLookingForLoggingInUser() {
        String username = "user";
        when(userRepository.findByEmailIgnoreCaseAndIsActiveTrue(username)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> customDetailsService.loadUserByUsername(username));
    }

}