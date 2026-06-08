package com.example.minibank.service.impl;

import com.example.minibank.dto.AuthResponse;
import com.example.minibank.dto.LoginRequest;
import com.example.minibank.dto.RegisterRequest;
import com.example.minibank.exception.ResourceNotFoundException;
import com.example.minibank.exception.UserAlreadyExistsException;
import com.example.minibank.model.Role;
import com.example.minibank.model.User;
import com.example.minibank.repository.UserRepository;
import com.example.minibank.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("ayman")
                .email("ayman@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest("ayman", "ayman@test.com", "password123");

        when(userRepository.existsByUsername("ayman")).thenReturn(false);
        when(userRepository.existsByEmail("ayman@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken("ayman@test.com")).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("ayman@test.com");
        assertThat(response.getUsername()).isEqualTo("ayman");
    }

    @Test
    void register_ThrowsException_WhenUsernameExists() {
        RegisterRequest req = new RegisterRequest("ayman", "ayman@test.com", "password123");

        when(userRepository.existsByUsername("ayman")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void register_ThrowsException_WhenEmailExists() {
        RegisterRequest req = new RegisterRequest("ayman", "ayman@test.com", "password123");

        when(userRepository.existsByUsername("ayman")).thenReturn(false);
        when(userRepository.existsByEmail("ayman@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest("ayman@test.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("ayman@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("ayman@test.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("ayman@test.com");
    }

    @Test
    void login_ThrowsException_WhenUserNotFound() {
        LoginRequest req = new LoginRequest("notfound@test.com", "password123");

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("notfound@test.com");
    }
}