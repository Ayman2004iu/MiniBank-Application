package com.example.minibank.service;

import com.example.minibank.dto.TransactionRequest;
import com.example.minibank.dto.TransactionResponse;



import java.util.List;


public interface TransactionService {
    TransactionResponse deposit(TransactionRequest dto, String email);
    TransactionResponse withdraw(TransactionRequest dto, String email);
    TransactionResponse transfer(TransactionRequest dto, String email);
    List<TransactionResponse> history(Long accountId);
    List<TransactionResponse> getAllHistory();
}
