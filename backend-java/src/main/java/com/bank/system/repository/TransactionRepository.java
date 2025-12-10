package com.bank.system.repository;

import com.bank.system.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByUserIdOrderByTimestampDesc(String userId);
    List<Transaction> findTop100ByOrderByTimestampDesc();
}
