package com.bank.system.repository;

import com.bank.system.entity.SubAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubAccountRepository extends JpaRepository<SubAccount, String> {
    List<SubAccount> findByUserIdOrderByCreatedAt(String userId);
    Optional<SubAccount> findByIdAndUserId(String id, String userId);
    long countByUserId(String userId);
}
