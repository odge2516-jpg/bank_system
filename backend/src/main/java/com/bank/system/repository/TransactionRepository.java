package com.bank.system.repository;

import com.bank.system.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByUserIdOrderByTimestampDesc(String userId);
    List<Transaction> findTop100ByOrderByTimestampDesc();
    
    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.user.id = ?1")
    void deleteByUserId(String userId);
}
