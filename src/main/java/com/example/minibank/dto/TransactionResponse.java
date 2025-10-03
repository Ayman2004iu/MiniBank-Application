package com.example.minibank.dto;


import com.example.minibank.model.TransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    public Long id;
    public TransactionType type;
    public BigDecimal amount;
    public String note;
    public LocalDateTime timestamp;
}
