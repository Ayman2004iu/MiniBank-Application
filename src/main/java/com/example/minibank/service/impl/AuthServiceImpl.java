package com.example.minibank.service.impl;

import com.example.minibank.dto.AuthResponse;
import com.example.minibank.dto.LoginRequest;
import com.example.minibank.dto.RegisterRequest;
import com.example.minibank.model.Role;
import com.example.minibank.model.User;
import com.example.minibank.repository.UserRepository;
import com.example.minibank.security.JwtUtil;
import com.example.minibank.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    public AuthServiceImpl(UserRepository userRepository,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username)) throw new RuntimeException("Username taken");

        if (userRepository.existsByEmail(req.email)) throw new RuntimeException("Email taken");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
    }


    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return buildAuthResponse(user);
    }
    private AuthResponse buildAuthResponse(User user) {
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getUsername(), user.getEmail());
    }
}
