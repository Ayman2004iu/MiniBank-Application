package com.example.minibank.service.impl;

import com.example.minibank.dto.TransactionRequest;
import com.example.minibank.dto.TransactionResponse;
import com.example.minibank.exception.DailyTransferLimitExceededException;
import com.example.minibank.exception.InsufficientFundsException;
import com.example.minibank.exception.SameAccountTransferException;
import com.example.minibank.exception.TransactionLimitExceededException;
import com.example.minibank.exception.UnauthorizedAccountAccessException;
import com.example.minibank.model.Account;
import com.example.minibank.model.Role;
import com.example.minibank.model.TransactionRecord;
import com.example.minibank.model.TransactionType;
import com.example.minibank.model.User;
import com.example.minibank.repository.AccountRepository;
import com.example.minibank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Account account;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transactionService, "maxWithdrawAmount", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(transactionService, "maxTransferAmount", BigDecimal.valueOf(50000));
        ReflectionTestUtils.setField(transactionService, "maxDailyTransfers", 5);

        user = User.builder()
                .id(1L)
                .username("ayman")
                .email("ayman@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();

        account = Account.builder()
                .id(1L)
                .owner(user)
                .accountNumber("MB00000000000001")
                .balance(BigDecimal.valueOf(5000))
                .build();

        targetAccount = Account.builder()
                .id(2L)
                .owner(user)
                .accountNumber("MB00000000000002")
                .balance(BigDecimal.valueOf(1000))
                .build();
    }


    @Test
    void deposit_Success() {
        TransactionRequest req = new TransactionRequest("MB00000000000001", BigDecimal.valueOf(500), null);
        TransactionRecord savedTr = buildTransactionRecord(TransactionType.DEPOSIT, BigDecimal.valueOf(500));

        when(accountRepository.findByAccountNumberForUpdate("MB00000000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionRepository.save(any())).thenReturn(savedTr);

        TransactionResponse response = transactionService.deposit(req, "ayman@test.com");

        assertThat(response.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void deposit_ThrowsException_WhenUnauthorized() {
        TransactionRequest req = new TransactionRequest("MB00000000000001", BigDecimal.valueOf(500), null);

        when(accountRepository.findByAccountNumberForUpdate("MB00000000000001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.deposit(req, "other@test.com"))
                .isInstanceOf(UnauthorizedAccountAccessException.class);
    }


    @Test
    void withdraw_Success() {
        TransactionRequest req = new TransactionRequest("MB00000000000001", BigDecimal.valueOf(500), null);
        TransactionRecord savedTr = buildTransactionRecord(TransactionType.WITHDRAW, BigDecimal.valueOf(500));

        when(accountRepository.findByAccountNumberForUpdate("MB00000000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionRepository.save(any())).thenReturn(savedTr);

        TransactionResponse response = transactionService.withdraw(req, "ayman@test.com");

        assertThat(response.getType()).isEqualTo(TransactionType.WITHDRAW);
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void withdraw_ThrowsException_WhenInsufficientFunds() {
        TransactionRequest req = new TransactionRequest("MB00000000000001", BigDecimal.valueOf(9000), null);

        when(accountRepository.findByAccountNumberForUpdate("MB00000000000001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.withdraw(req, "ayman@test.com"))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void withdraw_ThrowsException_WhenExceedsLimit() {
        TransactionRequest req = new TransactionRequest("MB00000000000001", BigDecimal.valueOf(15000), null);

        when(accountRepository.findByAccountNumberForUpdate("MB00000000000001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transactionService.withdraw(req, "ayman@test.com"))
                .isInstanceOf(TransactionLimitExceededException.class)
                .hasMessageContaining("10000");
    }


    @Test
    void transfer_Success() {
        TransactionRequest req = new TransactionRequest("MB00000000000001", BigDecimal.valueOf(500), "MB00000000000002");
        TransactionRecord savedTr = buildTransactionRecord(TransactionType.TRANSFER, BigDecimal.valueOf(500));

        when(transactionRepository.countByAccountNumberAndTypeAndTimestampAfter(
                eq("MB00000000000001"), eq(TransactionType.TRANSFER), any())).thenReturn(0L);
        when(accountRepository.findByAccountNumberForUpdate("MB00000000000001")).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumberForUpdate("MB00000000000002")).thenReturn(Optional.of(targetAccount));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionRepository.save(any())).thenReturn(savedTr);

        TransactionResponse response = transactionService.transfer(req, "ayman@test.com");

        assertThat(response.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void transfer_ThrowsException_WhenSameAccount() {
        TransactionRequest req = new TransactionRequest("MB00000000000001", BigDecimal.valueOf(500), "MB00000000000001");

        assertThatThrownBy(() -> transactionService.transfer(req, "ayman@test.com"))
                .isInstanceOf(SameAccountTransferException.class);
    }

    @Test
    void transfer_ThrowsException_WhenExceedsAmountLimit() {
        TransactionRequest req = new TransactionRequest("MB00000000000001", BigDecimal.valueOf(60000), "MB00000000000002");

        assertThatThrownBy(() -> transactionService.transfer(req, "ayman@test.com"))
                .isInstanceOf(TransactionLimitExceededException.class)
                .hasMessageContaining("50000");
    }

    @Test
    void transfer_ThrowsException_WhenExceedsDailyLimit() {
        TransactionRequest req = new TransactionRequest("MB00000000000001", BigDecimal.valueOf(500), "MB00000000000002");

        when(transactionRepository.countByAccountNumberAndTypeAndTimestampAfter(
                eq("MB00000000000001"), eq(TransactionType.TRANSFER), any())).thenReturn(5L);

        assertThatThrownBy(() -> transactionService.transfer(req, "ayman@test.com"))
                .isInstanceOf(DailyTransferLimitExceededException.class)
                .hasMessageContaining("5");
    }


    private TransactionRecord buildTransactionRecord(TransactionType type, BigDecimal amount) {
        return TransactionRecord.builder()
                .id(1L)
                .account(account)
                .type(type)
                .amount(amount)
                .note(type.name().toLowerCase() + " operation")
                .build();
    }
}