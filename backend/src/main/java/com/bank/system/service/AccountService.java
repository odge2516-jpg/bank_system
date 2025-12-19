package com.bank.system.service;

import com.bank.system.dto.BankDTOs.SubAccountRequest;
import com.bank.system.entity.FavoriteAccount;
import com.bank.system.entity.FavoriteAccountId;
import com.bank.system.entity.SubAccount;
import com.bank.system.entity.User;
import com.bank.system.repository.FavoriteAccountRepository;
import com.bank.system.repository.SubAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService {

    @Autowired private SubAccountRepository subAccountRepository;
    @Autowired private FavoriteAccountRepository favoriteAccountRepository;

    public SubAccount createSubAccount(SubAccountRequest req) {
        SubAccount sub = new SubAccount();
        sub.setId("SUB" + System.currentTimeMillis());
        User user = new User();
        user.setId(req.getUserId());
        sub.setUser(user);
        sub.setName(req.getName() != null ? req.getName() : "新帳戶");
        sub.setBalance(0L);
        sub.setColor(req.getColor() != null ? req.getColor() : "#3b82f6");
        return subAccountRepository.save(sub);
    }

    public void deleteSubAccount(String subAccountId, String userId) {
        long count = subAccountRepository.countByUserId(userId);
        if (count <= 1) throw new RuntimeException("至少需要保留一個子帳戶");
        
        SubAccount sub = subAccountRepository.findById(subAccountId).orElseThrow();
        if (sub.getBalance() > 0L) {
            throw new RuntimeException("請先將此帳戶餘額轉出或提領完畢");
        }
        subAccountRepository.delete(sub);
    }
    
    public void deleteFavorite(String userId, String favoriteUserId) {
        FavoriteAccount fav = favoriteAccountRepository.findById(new FavoriteAccountId(userId, favoriteUserId))
            .orElseThrow(() -> new RuntimeException("常用帳號不存在"));
        favoriteAccountRepository.delete(fav);
    }
}
