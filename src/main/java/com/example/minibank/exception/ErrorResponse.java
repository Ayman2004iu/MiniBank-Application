package com.example.minibank.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        int statusCode,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {}
