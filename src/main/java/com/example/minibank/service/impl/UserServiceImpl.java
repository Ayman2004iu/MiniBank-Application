package com.example.minibank.service.impl;

import com.example.minibank.dto.UserResponse;
import com.example.minibank.repository.UserRepository;
import com.example.minibank.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size) {
        log.info("Fetching all users: page={} size={}", page, size);
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt())
                        .build());
    }
}