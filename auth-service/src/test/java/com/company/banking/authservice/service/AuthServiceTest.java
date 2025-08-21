package com.company.banking.authservice.service;

import com.company.banking.authservice.client.NotificationServiceFeignClient;
import com.company.banking.authservice.client.PersonServiceFeignClient;
import com.company.banking.authservice.dto.AuthRequest;
import com.company.banking.authservice.dto.AuthResponse;
import com.company.banking.authservice.dto.RegisterRequest;
import com.company.banking.authservice.model.User;
import com.company.banking.authservice.repository.UserRepository;
import com.company.banking.authservice.util.JwtUtil;
import com.company.common.dto.PersonDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PersonServiceFeignClient personServiceFeignClient;
    @Mock
    private NotificationServiceFeignClient notificationServiceFeignClient;
    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreatePersonAndUser() {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "password", "test@test.com", "Test", "User", "12345");
        PersonDTO personDTO = new PersonDTO(1L, "Test", "User", "test@test.com", "12345");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(personServiceFeignClient.createPerson(any(PersonDTO.class))).thenReturn(personDTO);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // When
        authService.register(request);

        // Then
        verify(personServiceFeignClient, times(1)).createPerson(any(PersonDTO.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(notificationServiceFeignClient, times(1)).sendNotification(any());
    }

    @Test
    void register_whenUsernameExists_shouldThrowException() {
        // Given
        RegisterRequest request = new RegisterRequest("testuser", "password", "test@test.com", "Test", "User", "12345");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User()));

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Username already taken");

        verify(personServiceFeignClient, never()).createPerson(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnAuthResponse() {
        // Given
        AuthRequest request = new AuthRequest("testuser", "password");
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testuser", "password", Collections.emptyList());
        String token = "test_token";

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);

        // When
        AuthResponse response = authService.login(request);

        // Then
        verify(authenticationManager, times(1)).authenticate(any());
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(token);
    }
}
