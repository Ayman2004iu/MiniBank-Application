package com.example.minibank.service.impl;

import com.example.minibank.dto.AccountRequest;
import com.example.minibank.dto.AccountResponse;
import com.example.minibank.exception.ResourceNotFoundException;
import com.example.minibank.exception.UnauthorizedAccountAccessException;
import com.example.minibank.model.Account;
import com.example.minibank.model.Role;
import com.example.minibank.model.User;
import com.example.minibank.repository.AccountRepository;
import com.example.minibank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
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
                .balance(BigDecimal.valueOf(500))
                .build();
    }

    @Test
    void createAccount_Success() {
        AccountRequest request = new AccountRequest(BigDecimal.valueOf(500));

        when(userRepository.findByEmail("ayman@test.com")).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountService.createAccount("ayman@test.com", request);

        assertThat(response).isNotNull();
        assertThat(response.getAccountNumber()).isEqualTo("MB00000000000001");
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void createAccount_ThrowsException_WhenBalanceLessThan100() {
        AccountRequest request = new AccountRequest(BigDecimal.valueOf(50));

        when(userRepository.findByEmail("ayman@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> accountService.createAccount("ayman@test.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100");
    }

    @Test
    void createAccount_ThrowsException_WhenUserNotFound() {
        AccountRequest request = new AccountRequest(BigDecimal.valueOf(500));

        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.createAccount("notfound@test.com", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAccount_Success() {
        when(accountRepository.findByAccountNumber("MB00000000000001")).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount("MB00000000000001", "ayman@test.com");

        assertThat(response.getAccountNumber()).isEqualTo("MB00000000000001");
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void getAccount_ThrowsException_WhenNotFound() {
        when(accountRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount("INVALID", "ayman@test.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("INVALID");
    }

    @Test
    void getAccount_ThrowsException_WhenNotOwner() {
        when(accountRepository.findByAccountNumber("MB00000000000001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.getAccount("MB00000000000001", "someoneelse@test.com"))
                .isInstanceOf(UnauthorizedAccountAccessException.class);
    }

    @Test
    void getAccountsByEmail_Success() {
        Account account2 = Account.builder()
                .id(2L)
                .owner(user)
                .accountNumber("MB00000000000002")
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(accountRepository.findByOwnerEmail("ayman@test.com"))
                .thenReturn(List.of(account, account2));

        List<AccountResponse> responses = accountService.getAccountsByEmail("ayman@test.com");

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getAccountNumber()).isEqualTo("MB00000000000001");
        assertThat(responses.get(1).getAccountNumber()).isEqualTo("MB00000000000002");
    }

    @Test
    void getAccountsByEmail_ReturnsEmpty_WhenNoAccounts() {
        when(accountRepository.findByOwnerEmail("ayman@test.com")).thenReturn(List.of());

        List<AccountResponse> responses = accountService.getAccountsByEmail("ayman@test.com");

        assertThat(responses).isEmpty();
    }

    @Test
    void getAllAccounts_Success() {
        Page<Account> accountPage = new PageImpl<>(List.of(account));
        when(accountRepository.findAll(any(Pageable.class))).thenReturn(accountPage);

        Page<AccountResponse> responses = accountService.getAllAccounts(0, 10);

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getAccountNumber()).isEqualTo("MB00000000000001");
    }
}