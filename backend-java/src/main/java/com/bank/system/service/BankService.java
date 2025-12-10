package com.bank.system.service;

import com.bank.system.dto.BankDTOs;
import com.bank.system.dto.BankDTOs.RegisterRequest;
import com.bank.system.dto.BankDTOs.LoginRequest;
import com.bank.system.dto.BankDTOs.TransactionRequest;
import com.bank.system.dto.BankDTOs.SubAccountRequest;
import com.bank.system.entity.*;
import com.bank.system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class BankService {

    @Autowired private UserRepository userRepository;
    @Autowired private SubAccountRepository subAccountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private FavoriteAccountRepository favoriteAccountRepository;

    public User register(RegisterRequest req) {
        if (userRepository.findByLoginId(req.getLoginId()).isPresent()) {
            throw new RuntimeException("此登入帳號已被註冊");
        }

        String bankAccountNumber;
        do {
            int part1 = 1000 + new Random().nextInt(9000);
            int part2 = 1000 + new Random().nextInt(9000);
            int part3 = 1000 + new Random().nextInt(9000);
            bankAccountNumber = "" + part1 + part2 + part3;
        } while (userRepository.existsById(bankAccountNumber));

        User user = new User();
        user.setId(bankAccountNumber);
        user.setLoginId(req.getLoginId());
        user.setRealName(req.getRealName());
        user.setPassword(req.getPassword());
        user.setRole("user");
        user.setStatus("active");
        userRepository.save(user);

        SubAccount subAccount = new SubAccount();
        subAccount.setId("SUB001_" + bankAccountNumber);
        subAccount.setUser(user);
        subAccount.setName("主帳戶");
        subAccount.setBalance(req.getInitialDeposit());
        subAccount.setColor("#3b82f6");
        subAccountRepository.save(subAccount);

        return user;
    }

    public User login(LoginRequest req) {
        User user = userRepository.findByLoginId(req.getLoginId())
                .orElseThrow(() -> new RuntimeException("帳號或密碼錯誤"));

        if (!user.getPassword().equals(req.getPassword())) {
            throw new RuntimeException("帳號或密碼錯誤");
        }

        if (!"admin".equals(user.getRole()) && "frozen".equals(user.getStatus())) {
            throw new RuntimeException("帳號已被凍結，請聯繫管理員");
        }

        return user;
    }

    public void deposit(TransactionRequest req) {
        SubAccount subAccount = subAccountRepository.findByIdAndUserId(req.getSubAccountId(), req.getUserId())
                .orElseThrow(() -> new RuntimeException("子帳戶不存在"));

        subAccount.setBalance(subAccount.getBalance().add(req.getAmount()));
        subAccountRepository.save(subAccount);

        recordTransaction(req.getUserId(), "存款", req.getAmount(), 
            "存入「" + subAccount.getName() + "」", req.getSubAccountId());
    }

    public void withdraw(TransactionRequest req) {
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

    private void recordTransaction(String userId, String type, BigDecimal amount, String note, String subAccountId) {
        Transaction tx = new Transaction();
        tx.setId("TXN" + System.currentTimeMillis() + new Random().nextInt(1000));
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
    
    // Other methods (sub-accounts, admin, etc.) can be added here...
    // For brevity, I'll implement the basics first.
    
    public SubAccount createSubAccount(SubAccountRequest req) {
        SubAccount sub = new SubAccount();
        sub.setId("SUB" + System.currentTimeMillis());
        User user = new User();
        user.setId(req.getUserId());
        sub.setUser(user);
        sub.setName(req.getName() != null ? req.getName() : "新帳戶");
        sub.setBalance(BigDecimal.ZERO);
        sub.setColor(req.getColor() != null ? req.getColor() : "#3b82f6");
        return subAccountRepository.save(sub);
    }
    
    public void deleteSubAccount(String subAccountId, String userId) {
        long count = subAccountRepository.countByUserId(userId);
        if (count <= 1) throw new RuntimeException("至少需要保留一個子帳戶");
        
        SubAccount sub = subAccountRepository.findById(subAccountId).orElseThrow();
        if (sub.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("請先將此帳戶餘額轉出或提領完畢");
        }
        subAccountRepository.delete(sub);
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
}
