package com.example.minibank.service.impl;

import com.example.minibank.dto.AccountRequest;
import com.example.minibank.dto.AccountResponse;
import com.example.minibank.exception.ResourceNotFoundException;
import com.example.minibank.model.Account;
import com.example.minibank.model.User;
import com.example.minibank.repository.AccountRepository;
import com.example.minibank.repository.UserRepository;
import com.example.minibank.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AccountResponse createAccount(String email, AccountRequest request) {
        log.info("Creating account for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (request.getBalance().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new IllegalArgumentException("Minimum opening balance is 100");
        }

        String accNum = generateUniqueAccountNumber();

        Account account = Account.builder()
                .owner(user)
                .accountNumber(accNum)
                .balance(request.getBalance())
                .build();

        Account saved = accountRepository.save(account);

        log.info("Account created successfully: accountNumber={} user={}", accNum, email);
        return mapToResponse(saved);
    }

    @Override
    public AccountResponse getAccount(String accountNumber) {
        Account acc = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return mapToResponse(acc);
    }

    @Override
    public List<AccountResponse> getAccountsByEmail(String email) {
        log.info("Fetching all accounts for user: {}", email);
        return accountRepository.findByOwnerEmail(email).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Page<AccountResponse> getAllAccounts(int page, int size) {
        return accountRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::mapToResponse);
    }

    private String generateUniqueAccountNumber() {
        String accNum;
        do {
            accNum = "MB" + String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits()));
        } while (accountRepository.existsByAccountNumber(accNum));
        return accNum;
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .createdAt(account.getCreatedAt())
                .balance(account.getBalance())
                .build();
    }
}