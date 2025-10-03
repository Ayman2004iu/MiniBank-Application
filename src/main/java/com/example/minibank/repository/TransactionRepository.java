package com.example.minibank.repository;

import com.example.minibank.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
    List<TransactionRecord> findByAccountId(Long accountId);
}
