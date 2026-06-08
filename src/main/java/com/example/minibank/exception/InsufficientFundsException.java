package com.example.minibank.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountNumber, BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient funds in account %s: available %.2f, requested %.2f",
                accountNumber, available, requested));
    }
}