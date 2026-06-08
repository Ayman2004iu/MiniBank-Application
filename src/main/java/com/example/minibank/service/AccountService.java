package com.example.minibank.service;

import com.example.minibank.dto.AccountRequest;
import com.example.minibank.dto.AccountResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(String email, AccountRequest request);
    AccountResponse getAccount(String accountNumber);
    List<AccountResponse> getAccountsByEmail(String email);
    Page<AccountResponse> getAllAccounts(int page, int size);
}