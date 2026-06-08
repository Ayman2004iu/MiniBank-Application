package com.example.minibank.exception;

import java.math.BigDecimal;

public class TransactionLimitExceededException extends RuntimeException {
    public TransactionLimitExceededException(String type, BigDecimal limit) {
        super("Transaction limit exceeded: maximum amount for " + type + " is " + limit);
    }
}