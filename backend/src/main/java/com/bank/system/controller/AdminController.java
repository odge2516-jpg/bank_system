package com.bank.system.controller;

import com.bank.system.entity.SubAccount;
import com.bank.system.entity.Transaction;
import com.bank.system.entity.User;
import com.bank.system.repository.FavoriteAccountRepository;
import com.bank.system.repository.SubAccountRepository;
import com.bank.system.repository.TransactionRepository;
import com.bank.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SubAccountRepository subAccountRepository;
    @Autowired private FavoriteAccountRepository favoriteAccountRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (User user : users) {
            // Skip admin users - only show regular users
            if (user.getRole() == com.bank.system.enums.UserRole.admin) {
                continue;
            }
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("loginId", user.getLoginId());
            userMap.put("realName", user.getRealName());
            userMap.put("role", user.getRole());
            userMap.put("status", user.getStatus());
            userMap.put("createdAt", user.getCreatedAt());
            
            // Calculate balance from sub-accounts
            List<SubAccount> subAccounts = subAccountRepository.findByUserIdOrderByCreatedAt(user.getId());
            BigDecimal totalBalance = subAccounts.stream()
                    .map(SubAccount::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            userMap.put("balance", totalBalance);
            
            result.add(userMap);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Transaction tx : transactions) {
            Map<String, Object> txMap = new HashMap<>();
            txMap.put("id", tx.getId());
            txMap.put("userId", tx.getUser().getId());  // Expose userId
            txMap.put("type", tx.getType());
            txMap.put("amount", tx.getAmount());
            txMap.put("note", tx.getNote());
            txMap.put("time", tx.getTime());
            txMap.put("timestamp", tx.getTimestamp());
            txMap.put("subAccountId", tx.getSubAccountId());
            result.add(txMap);
        }
        
        return ResponseEntity.ok(result);
    }
    
    @Transactional
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "用戶不存在"));
        }
        
        // Don't allow deleting admin users
        if (user.getRole() == com.bank.system.enums.UserRole.admin) {
            return ResponseEntity.status(403).body(Map.of("error", "無法刪除管理員帳號"));
        }
        
        // Delete related data first (foreign key constraints)
        transactionRepository.deleteByUserId(userId);
        favoriteAccountRepository.deleteByUserId(userId);
        subAccountRepository.deleteByUserId(userId);
        userRepository.delete(user);
        
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "用戶不存在"));
        }
        
        // Toggle status
        if (user.getStatus() == com.bank.system.enums.UserStatus.active) {
            user.setStatus(com.bank.system.enums.UserStatus.frozen);
        } else {
            user.setStatus(com.bank.system.enums.UserStatus.active);
        }
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of("success", true, "status", user.getStatus()));
    }
}

