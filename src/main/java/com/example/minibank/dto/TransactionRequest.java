package com.example.minibank.dto;

import lombok.*;
import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    public String accountNumber;
    public BigDecimal amount;
    public String targetAccountNumber;



}
