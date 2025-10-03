package com.example.minibank.service.impl;

import com.example.minibank.dto.TransactionRequest;
import com.example.minibank.dto.TransactionResponse;
import com.example.minibank.model.Account;
import com.example.minibank.model.TransactionRecord;
import com.example.minibank.model.TransactionType;
import com.example.minibank.repository.AccountRepository;
import com.example.minibank.repository.TransactionRepository;
import com.example.minibank.repository.UserRepository;
import com.example.minibank.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;




@Service
public class TransactionServiceImpl implements TransactionService {


    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;


    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;

    }


    @Override
    @Transactional
    public TransactionResponse deposit(TransactionRequest dto, String email) {

        Account acc = accountRepository.findByAccountNumber(dto.accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!acc.getOwner().getEmail().equals(email)) throw new RuntimeException("This account does not belong to the current user");

        acc.setBalance(acc.getBalance().add(dto.amount));

        accountRepository.save(acc);

        TransactionRecord tr = TransactionRecord.builder()
                .account(acc)
                .type(TransactionType.DEPOSIT)
                .amount(dto.amount)
                .note("Deposit operation")
                .build();
        tr = transactionRepository.save(tr);

        return mapToResponse(tr);
    }


    @Override
    @Transactional
    public TransactionResponse withdraw(TransactionRequest dto, String email) {
        Account acc = accountRepository.findByAccountNumber(dto.accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!acc.getOwner().getEmail().equals(email)) throw new RuntimeException("This account does not belong to the current user");

        if (acc.getBalance().compareTo(dto.amount) < 0) throw new RuntimeException("Insufficient funds");

        acc.setBalance(acc.getBalance().subtract(dto.amount));

        accountRepository.save(acc);

        TransactionRecord tr = TransactionRecord.builder()
                .account(acc)
                .type(TransactionType.WITHDRAW)
                .amount(dto.amount)
                .note("Withdraw operation")
                .build();
        tr = transactionRepository.save(tr);

        return mapToResponse(tr);
    }
    @Override
    @Transactional
    public TransactionResponse transfer(TransactionRequest dto, String email) {
        Account from = accountRepository.findByAccountNumber(dto.accountNumber)
                .orElseThrow(() -> new RuntimeException("From account not found"));

        if (!from.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("This account does not belong to the current user");
        }

        Account to = accountRepository.findByAccountNumber(dto.targetAccountNumber)
                .orElseThrow(() -> new RuntimeException("Target account not found"));

        if (from.getBalance().compareTo(dto.amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(dto.amount));
        to.setBalance(to.getBalance().add(dto.amount));

        accountRepository.save(from);
        accountRepository.save(to);

        TransactionRecord tr = TransactionRecord.builder()
                .account(from)
                .type(TransactionType.TRANSFER)
                .amount(dto.amount)
                .note("to:" + to.getAccountNumber())
                .build();
        tr = transactionRepository.save(tr);

        return mapToResponse(tr);
    }


    @Override
    public List<TransactionResponse> history(Long accountId) {
          return transactionRepository.findByAccountId(accountId)
                  .stream()
                  .map(this::mapToResponse)
                  .toList();
    }
    @Override
    public List<TransactionResponse> getAllHistory() {
        return transactionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TransactionResponse mapToResponse(TransactionRecord transactionRecord){
        return TransactionResponse.builder()
                .id(transactionRecord.getId())
                .type(transactionRecord.getType())
                .amount(transactionRecord.getAmount())
                .note(transactionRecord.getNote())
                .timestamp(transactionRecord.getTimestamp())
                .build();

    }
}