package com.example.minibank.service;

import com.example.minibank.dto.UserResponse;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserResponse> getAllUsers(int page, int size);
}