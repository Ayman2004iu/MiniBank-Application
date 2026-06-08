package com.example.minibank.repository;

import com.example.minibank.model.TransactionRecord;
import com.example.minibank.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {

    Page<TransactionRecord> findByAccount_AccountNumber(String accountNumber, Pageable pageable);

    @Query("SELECT COUNT(t) FROM TransactionRecord t WHERE t.account.accountNumber = :accountNumber " +
            "AND t.type = :type AND t.timestamp >= :since")
    long countByAccountNumberAndTypeAndTimestampAfter(
            @Param("accountNumber") String accountNumber,
            @Param("type") TransactionType type,
            @Param("since") LocalDateTime since);
}