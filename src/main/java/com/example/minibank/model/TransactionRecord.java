package com.example.minibank.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;


    @Column(nullable = false)
    private BigDecimal amount;


    private String note;


    private LocalDateTime timestamp;


    @PrePersist
    public void prePersist() {

        timestamp = LocalDateTime.now();
    }
}
