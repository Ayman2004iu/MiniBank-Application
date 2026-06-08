package com.example.minibank.dto;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotNull(message = "Initial balance is required")
    @PositiveOrZero(message = "Balance must be zero or positive")
    private BigDecimal balance;
}