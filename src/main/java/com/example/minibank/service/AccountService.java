package com.example.minibank.service;

import com.example.minibank.dto.AccountRequest;
import com.example.minibank.dto.AccountResponse;


public interface AccountService {
    AccountResponse createAccount(String email , AccountRequest request);
    AccountResponse getAccount(String accountNumber);
}
