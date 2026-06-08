package com.example.minibank.exception;

public class DailyTransferLimitExceededException extends RuntimeException {
    public DailyTransferLimitExceededException(int limit) {
        super("Daily transfer limit exceeded: maximum " + limit + " transfers per day");
    }
}