package com.example.minibank.controller;



import com.example.minibank.dto.AccountRequest;
import com.example.minibank.dto.AccountResponse;
import com.example.minibank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/accounts")
public class AccountController {


    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    @PostMapping
    public ResponseEntity<AccountResponse> createAccount( Authentication auth ,@Valid @RequestBody AccountRequest request) {
        String email = auth.getName();
        AccountResponse dto = accountService.createAccount(email , request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }


    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }
}
