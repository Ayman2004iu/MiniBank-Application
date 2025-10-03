package com.example.minibank.service;

import com.example.minibank.dto.AuthResponse;
import com.example.minibank.dto.LoginRequest;
import com.example.minibank.dto.RegisterRequest;



public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
}
