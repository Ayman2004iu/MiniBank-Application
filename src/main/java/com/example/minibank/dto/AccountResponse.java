package com.example.minibank.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    public Long id;
    public String accountNumber;
    public BigDecimal balance;
    private LocalDateTime createdAt;
}
