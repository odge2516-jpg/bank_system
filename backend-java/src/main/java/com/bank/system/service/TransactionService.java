package com.bank.system.service;

import com.bank.system.dto.BankDTOs.TransactionRequest;
import com.bank.system.entity.FavoriteAccount;
import com.bank.system.entity.SubAccount;
import com.bank.system.entity.Transaction;
import com.bank.system.entity.User;
import com.bank.system.repository.FavoriteAccountRepository;
import com.bank.system.repository.SubAccountRepository;
import com.bank.system.repository.TransactionRepository;
import com.bank.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    @Autowired private SubAccountRepository subAccountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private FavoriteAccountRepository favoriteAccountRepository;

    public void deposit(TransactionRequest req) {
        // Check if user account is frozen
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));
        if (user.getStatus() == com.bank.system.enums.UserStatus.frozen) {
            throw new RuntimeException("帳戶已被凍結");
        }
        
        SubAccount subAccount = subAccountRepository.findByIdAndUserId(req.getSubAccountId(), req.getUserId())
                .orElseThrow(() -> new RuntimeException("子帳戶不存在"));

        subAccount.setBalance(subAccount.getBalance().add(req.getAmount()));
        subAccountRepository.save(subAccount);

        recordTransaction(req.getUserId(), "存款", req.getAmount(), 
            "存入「" + subAccount.getName() + "」", req.getSubAccountId());
    }

    public void withdraw(TransactionRequest req) {
        // Check if user account is frozen
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));
        if (user.getStatus() == com.bank.system.enums.UserStatus.frozen) {
            throw new RuntimeException("帳戶已被凍結");
        }
        
        SubAccount subAccount = subAccountRepository.findByIdAndUserId(req.getSubAccountId(), req.getUserId())
                .orElseThrow(() -> new RuntimeException("子帳戶不存在"));

        if (subAccount.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("子帳戶餘額不足");
        }

        subAccount.setBalance(subAccount.getBalance().subtract(req.getAmount()));
        subAccountRepository.save(subAccount);

        recordTransaction(req.getUserId(), "提款", req.getAmount().negate(), 
            "從「" + subAccount.getName() + "」提領", req.getSubAccountId());
    }

    public void transfer(TransactionRequest req) {
        String cleanAccount = req.getRecipientAccountNumber().replace("-", "").replace(" ", "");
        
        if (cleanAccount.equals(req.getUserId())) {
            throw new RuntimeException("不能轉帳給自己");
        }
        
        // Check if sender account is frozen
        User sender = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));
        if (sender.getStatus() == com.bank.system.enums.UserStatus.frozen) {
            throw new RuntimeException("帳戶已被凍結");
        }

        User recipient = userRepository.findById(cleanAccount)
                .orElseThrow(() -> new RuntimeException("收款帳號不存在"));

        List<SubAccount> senderAccounts = subAccountRepository.findByUserIdOrderByCreatedAt(req.getUserId());
        if (senderAccounts.isEmpty()) throw new RuntimeException("找不到付款帳戶");
        SubAccount senderAccount = senderAccounts.get(0);

        if (senderAccount.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("餘額不足");
        }

        List<SubAccount> recipientAccounts = subAccountRepository.findByUserIdOrderByCreatedAt(recipient.getId());
        if (recipientAccounts.isEmpty()) throw new RuntimeException("找不到收款帳戶");
        SubAccount recipientAccount = recipientAccounts.get(0);

        senderAccount.setBalance(senderAccount.getBalance().subtract(req.getAmount()));
        recipientAccount.setBalance(recipientAccount.getBalance().add(req.getAmount()));
        
        subAccountRepository.save(senderAccount);
        subAccountRepository.save(recipientAccount);

        if (req.isSaveAsFavorite()) {
            FavoriteAccount fav = new FavoriteAccount();
            fav.setUserId(req.getUserId());
            fav.setFavoriteUserId(cleanAccount);
            favoriteAccountRepository.save(fav);
        }

        String maskAccount = cleanAccount.substring(0, 4) + "****" + cleanAccount.substring(cleanAccount.length() - 4);
        recordTransaction(req.getUserId(), "轉帳支出", req.getAmount().negate(), 
            "轉給 " + maskAccount, null);

        String maskSender = req.getUserId().substring(0, 4) + "****" + req.getUserId().substring(req.getUserId().length() - 4);
        recordTransaction(cleanAccount, "轉帳收入", req.getAmount(), 
            "來自 " + maskSender, null);
    }
    
    public void transferBetweenSubAccounts(TransactionRequest req) {
        SubAccount from = subAccountRepository.findByIdAndUserId(req.getFromSubAccountId(), req.getUserId()).orElseThrow();
        SubAccount to = subAccountRepository.findByIdAndUserId(req.getToSubAccountId(), req.getUserId()).orElseThrow();
        
        if (from.getBalance().compareTo(req.getAmount()) < 0) throw new RuntimeException("子帳戶餘額不足");
        
        from.setBalance(from.getBalance().subtract(req.getAmount()));
        to.setBalance(to.getBalance().add(req.getAmount()));
        
        subAccountRepository.save(from);
        subAccountRepository.save(to);
        
        recordTransaction(req.getUserId(), "內部轉帳", BigDecimal.ZERO, 
            "從「" + from.getName() + "」轉至「" + to.getName() + "」NT$ " + req.getAmount(), null);
    }

    private void recordTransaction(String userId, String type, BigDecimal amount, String note, String subAccountId) {
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID().toString());
        User user = new User();
        user.setId(userId);
        tx.setUser(user);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setNote(note);
        tx.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd a h:mm:ss", Locale.TAIWAN)));
        tx.setTimestamp(System.currentTimeMillis());
        tx.setSubAccountId(subAccountId);
        transactionRepository.save(tx);
    }
}
