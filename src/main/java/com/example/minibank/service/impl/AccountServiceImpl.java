package com.example.minibank.service.impl;

import com.example.minibank.dto.AccountRequest;
import com.example.minibank.dto.AccountResponse;
import com.example.minibank.model.Account;
import com.example.minibank.model.User;
import com.example.minibank.repository.AccountRepository;
import com.example.minibank.repository.UserRepository;
import com.example.minibank.service.AccountService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;


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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getBalance().compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("You must pay 100 to open the account");
        }

        String accNum = generateAccountNumber();
        Account account = Account.builder()
                .owner(user)
                .accountNumber(accNum)
                .balance(BigDecimal.ZERO)
                .build();

        Account saved = accountRepository.save(account);

        return mapToResponse(saved);
    }


    @Override
    public AccountResponse getAccount(String accountNumber) {
        Account acc = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return mapToResponse(acc);
    }


    private String generateAccountNumber() {
        return  "MB" + String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits()));
    }

    private AccountResponse mapToResponse(Account account){
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .createdAt(account.getCreatedAt())
                .balance(account.getBalance())
                .build();
    }
}
