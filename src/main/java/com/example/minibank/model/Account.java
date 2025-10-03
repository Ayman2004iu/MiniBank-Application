package com.example.minibank.model;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;


    @Column(unique = true, nullable = false)
    private String accountNumber;


    @Column(nullable = false)
    private BigDecimal balance;


    private LocalDateTime createdAt= LocalDateTime.now();


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
