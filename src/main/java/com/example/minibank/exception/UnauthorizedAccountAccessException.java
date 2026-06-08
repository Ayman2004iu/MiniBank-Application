package com.example.minibank.exception;

public class UnauthorizedAccountAccessException extends RuntimeException {
    public UnauthorizedAccountAccessException(String accountNumber) {
        super("Access denied: account " + accountNumber + " does not belong to the current user");
    }
}