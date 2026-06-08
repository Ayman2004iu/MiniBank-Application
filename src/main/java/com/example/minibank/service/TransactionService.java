package com.example.minibank.service;

import com.example.minibank.dto.TransactionRequest;
import com.example.minibank.dto.TransactionResponse;
import org.springframework.data.domain.Page;

public interface TransactionService {
    TransactionResponse deposit(TransactionRequest dto, String email);
    TransactionResponse withdraw(TransactionRequest dto, String email);
    TransactionResponse transfer(TransactionRequest dto, String email);
    Page<TransactionResponse> history(String accountNumber, String email, int page, int size);
    Page<TransactionResponse> getAllHistory(int page, int size);
}