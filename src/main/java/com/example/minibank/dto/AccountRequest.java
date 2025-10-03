package com.example.minibank.dto;


import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {
    public BigDecimal balance;
}
