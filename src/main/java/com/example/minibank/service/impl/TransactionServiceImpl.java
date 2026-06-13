package com.example.minibank.service.impl;

import com.example.minibank.dto.TransactionRequest;
import com.example.minibank.dto.TransactionResponse;
import com.example.minibank.exception.DailyTransferLimitExceededException;
import com.example.minibank.exception.InsufficientFundsException;
import com.example.minibank.exception.ResourceNotFoundException;
import com.example.minibank.exception.SameAccountTransferException;
import com.example.minibank.exception.TransactionLimitExceededException;
import com.example.minibank.exception.UnauthorizedAccountAccessException;
import com.example.minibank.model.Account;
import com.example.minibank.model.TransactionRecord;
import com.example.minibank.model.TransactionType;
import com.example.minibank.repository.AccountRepository;
import com.example.minibank.repository.TransactionRepository;
import com.example.minibank.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Value("${app.transaction.limits.max-withdraw-amount}")
    private BigDecimal maxWithdrawAmount;

    @Value("${app.transaction.limits.max-transfer-amount}")
    private BigDecimal maxTransferAmount;

    @Value("${app.transaction.limits.max-daily-transfers}")
    private int maxDailyTransfers;

    public TransactionServiceImpl(AccountRepository accountRepository,
                                  TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public TransactionResponse deposit(TransactionRequest dto, String email) {
        log.info("Deposit attempt: amount={} account={} user={}", dto.getAmount(), dto.getAccountNumber(), email);

        Account acc = findAccountForUpdate(dto.getAccountNumber());
        validateOwnership(acc, email, dto.getAccountNumber());

        acc.setBalance(acc.getBalance().add(dto.getAmount()));
        accountRepository.save(acc);

        TransactionRecord tr = saveTransaction(acc, TransactionType.DEPOSIT, dto.getAmount(), "Deposit operation");

        log.info("Deposit successful: amount={} account={}", dto.getAmount(), dto.getAccountNumber());
        return mapToResponse(tr);
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(TransactionRequest dto, String email) {
        log.info("Withdraw attempt: amount={} account={} user={}", dto.getAmount(), dto.getAccountNumber(), email);

        Account acc = findAccountForUpdate(dto.getAccountNumber());
        validateOwnership(acc, email, dto.getAccountNumber());

        if (dto.getAmount().compareTo(maxWithdrawAmount) > 0) {
            throw new TransactionLimitExceededException("WITHDRAW", maxWithdrawAmount);
        }

        validateBalance(acc, dto);

        acc.setBalance(acc.getBalance().subtract(dto.getAmount()));
        accountRepository.save(acc);

        TransactionRecord tr = saveTransaction(acc, TransactionType.WITHDRAW, dto.getAmount(), "Withdraw operation");

        log.info("Withdraw successful: amount={} account={}", dto.getAmount(), dto.getAccountNumber());
        return mapToResponse(tr);
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransactionRequest dto, String email) {
        log.info("Transfer attempt: amount={} from={} to={} user={}",
                dto.getAmount(), dto.getAccountNumber(), dto.getTargetAccountNumber(), email);

        if (dto.getTargetAccountNumber() == null || dto.getTargetAccountNumber().isBlank()) {
            throw new IllegalArgumentException("Target account number is required for transfer");
        }

        if (dto.getAccountNumber().equals(dto.getTargetAccountNumber())) {
            throw new SameAccountTransferException();
        }

        if (dto.getAmount().compareTo(maxTransferAmount) > 0) {
            throw new TransactionLimitExceededException("TRANSFER", maxTransferAmount);
        }

        long todayTransfers = transactionRepository.countByAccountNumberAndTypeAndTimestampAfter(
                dto.getAccountNumber(), TransactionType.TRANSFER, LocalDateTime.now().toLocalDate().atStartOfDay());

        if (todayTransfers >= maxDailyTransfers) {
            throw new DailyTransferLimitExceededException(maxDailyTransfers);
        }

        Account from;
        Account to;

        if (dto.getAccountNumber().compareTo(dto.getTargetAccountNumber()) < 0) {
            from = findAccountForUpdate(dto.getAccountNumber());
            to = findAccountForUpdate(dto.getTargetAccountNumber());
        } else {
            to = findAccountForUpdate(dto.getTargetAccountNumber());
            from = findAccountForUpdate(dto.getAccountNumber());
        }

        validateOwnership(from, email, dto.getAccountNumber());
        validateBalance(from, dto);

        from.setBalance(from.getBalance().subtract(dto.getAmount()));
        to.setBalance(to.getBalance().add(dto.getAmount()));

        accountRepository.save(from);
        accountRepository.save(to);

        TransactionRecord tr = saveTransaction(from, TransactionType.TRANSFER, dto.getAmount(),
                "to:" + to.getAccountNumber());

        log.info("Transfer successful: amount={} from={} to={}", dto.getAmount(), dto.getAccountNumber(), dto.getTargetAccountNumber());
        return mapToResponse(tr);
    }

    @Override
    public Page<TransactionResponse> history(String accountNumber, String email, int page, int size) {
        Account acc = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        validateOwnership(acc, email, accountNumber);

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return transactionRepository.findByAccount_AccountNumber(accountNumber, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<TransactionResponse> getAllHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return transactionRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private Account findAccountForUpdate(String accountNumber) {
        return accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
    }

    private void validateOwnership(Account acc, String email, String accountNumber) {
        if (!acc.getOwner().getEmail().equals(email)) {
            log.warn("Unauthorized access attempt: account={} user={}", accountNumber, email);
            throw new UnauthorizedAccountAccessException(accountNumber);
        }
    }

    private void validateBalance(Account acc, TransactionRequest dto) {
        if (acc.getBalance().compareTo(dto.getAmount()) < 0) {
            log.warn("Insufficient funds: account={} available={} requested={}",
                    acc.getAccountNumber(), acc.getBalance(), dto.getAmount());
            throw new InsufficientFundsException(acc.getAccountNumber(), acc.getBalance(), dto.getAmount());
        }
    }

    private TransactionRecord saveTransaction(Account acc, TransactionType type, BigDecimal amount, String note) {
        TransactionRecord tr = TransactionRecord.builder()
                .account(acc)
                .type(type)
                .amount(amount)
                .note(note)
                .build();
        return transactionRepository.save(tr);
    }

    private TransactionResponse mapToResponse(TransactionRecord tr) {
        return TransactionResponse.builder()
                .id(tr.getId())
                .type(tr.getType())
                .amount(tr.getAmount())
                .note(tr.getNote())
                .timestamp(tr.getTimestamp())
                .build();
    }
}