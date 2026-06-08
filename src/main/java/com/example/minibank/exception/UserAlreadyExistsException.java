package com.example.minibank.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String field, String value) {
        super(field + " already taken: " + value);
    }
}