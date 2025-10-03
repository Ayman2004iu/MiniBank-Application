package com.example.minibank.controller;

import com.example.minibank.dto.TransactionRequest;
import com.example.minibank.dto.TransactionResponse;
import com.example.minibank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {


    private final TransactionService transactionService;


    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest dto, Authentication auth) {
        String email = auth.getName();
        TransactionResponse tr = transactionService.deposit(dto, email);
        return ResponseEntity.ok(tr);
    }


    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody TransactionRequest dto, Authentication auth) {
        String email = auth.getName();
        TransactionResponse tr = transactionService.withdraw(dto, email);
        return ResponseEntity.ok(tr);
    }


    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransactionRequest dto, Authentication auth) {
        String email = auth.getName();
        TransactionResponse tr = transactionService.transfer(dto, email);
        return ResponseEntity.ok(tr);
    }


    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionResponse>> history(@PathVariable Long accountId) {

        return ResponseEntity.ok(transactionService.history(accountId));
    }

    @GetMapping("/history/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getAllHistory() {

        return ResponseEntity.ok(transactionService.getAllHistory());
    }
}