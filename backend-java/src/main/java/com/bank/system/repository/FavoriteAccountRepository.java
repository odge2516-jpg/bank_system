package com.bank.system.repository;

import com.bank.system.entity.FavoriteAccount;
import com.bank.system.entity.FavoriteAccountId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteAccountRepository extends JpaRepository<FavoriteAccount, FavoriteAccountId> {
    List<FavoriteAccount> findByUserId(String userId);
    void deleteByUserIdAndFavoriteUserId(String userId, String favoriteUserId);
}
