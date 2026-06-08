package com.example.minibank.controller;

import com.example.minibank.dto.TransactionRequest;
import com.example.minibank.dto.TransactionResponse;
import com.example.minibank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody TransactionRequest dto,
                                                       Authentication auth) {
        return ResponseEntity.ok(transactionService.deposit(dto, auth.getName()));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody TransactionRequest dto,
                                                        Authentication auth) {
        return ResponseEntity.ok(transactionService.withdraw(dto, auth.getName()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransactionRequest dto,
                                                        Authentication auth) {
        return ResponseEntity.ok(transactionService.transfer(dto, auth.getName()));
    }

    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<Page<TransactionResponse>> history(
            @PathVariable String accountNumber,
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 100);
        return ResponseEntity.ok(transactionService.history(accountNumber, auth.getName(), page, size));
    }

    @GetMapping("/history/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getAllHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 100);
        return ResponseEntity.ok(transactionService.getAllHistory(page, size));
    }
}