package com.bank.system.controller;

import com.bank.system.dto.BankDTOs;
import com.bank.system.dto.BankDTOs.RegisterRequest;
import com.bank.system.dto.BankDTOs.LoginRequest;
import com.bank.system.dto.BankDTOs.TransactionRequest;
import com.bank.system.dto.BankDTOs.SubAccountRequest;
import com.bank.system.dto.BankDTOs.FavoriteAccountRequest;
import com.bank.system.entity.*;
import com.bank.system.repository.*;
import com.bank.system.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BankController {

    @Autowired private BankService bankService;
    @Autowired private UserRepository userRepository;
    @Autowired private SubAccountRepository subAccountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private FavoriteAccountRepository favoriteAccountRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User user = bankService.register(req);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = bankService.login(req);
            
            // Fetch sub-accounts and favorites manually to populate response if needed, 
            // or rely on JSON serialization if Lazy loading is handled (Open Session In View).
            // Here we construct a clean response map similar to Node.js
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("loginId", user.getLoginId());
            userMap.put("realName", user.getRealName());
            userMap.put("role", user.getRole());
            userMap.put("status", user.getStatus());
            userMap.put("createdAt", user.getCreatedAt());
            
            List<SubAccount> subs = subAccountRepository.findByUserIdOrderByCreatedAt(user.getId());
            userMap.put("subAccounts", subs);
            
            List<FavoriteAccount> favs = favoriteAccountRepository.findByUserId(user.getId());
            userMap.put("favoriteAccounts", favs.stream().map(FavoriteAccount::getFavoriteUserId).collect(Collectors.toList()));

            return ResponseEntity.ok(Map.of("success", true, "user", userMap));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用戶不存在"));
            
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("loginId", user.getLoginId());
            userMap.put("realName", user.getRealName());
            userMap.put("role", user.getRole());
            userMap.put("status", user.getStatus());
            userMap.put("createdAt", user.getCreatedAt());
            
            List<SubAccount> subs = subAccountRepository.findByUserIdOrderByCreatedAt(user.getId());
            userMap.put("subAccounts", subs);
            
            List<FavoriteAccount> favs = favoriteAccountRepository.findByUserId(user.getId());
            userMap.put("favoriteAccounts", favs.stream().map(FavoriteAccount::getFavoriteUserId).collect(Collectors.toList()));

            return ResponseEntity.ok(userMap);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody TransactionRequest req) {
        try {
            bankService.deposit(req);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody TransactionRequest req) {
        try {
            bankService.withdraw(req);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransactionRequest req) {
        try {
            bankService.transfer(req);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<?> getTransactions(@PathVariable String userId) {
        return ResponseEntity.ok(transactionRepository.findByUserIdOrderByTimestampDesc(userId));
    }
    
    // Sub-account APIs
    @PostMapping("/sub-accounts")
    public ResponseEntity<?> createSubAccount(@RequestBody SubAccountRequest req) {
        try {
            SubAccount sub = bankService.createSubAccount(req);
            return ResponseEntity.ok(Map.of("success", true, "subAccount", sub));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/sub-accounts/{subAccountId}")
    public ResponseEntity<?> deleteSubAccount(@PathVariable String subAccountId, @RequestParam String userId) {
        try {
            bankService.deleteSubAccount(subAccountId, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/sub-accounts/transfer")
    public ResponseEntity<?> transferBetweenSubAccounts(@RequestBody TransactionRequest req) {
        try {
            bankService.transferBetweenSubAccounts(req);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    // Favorite Accounts
    @PostMapping("/favorite-accounts")
    public ResponseEntity<?> addFavorite(@RequestBody FavoriteAccountRequest req) {
        try {
            FavoriteAccount fav = new FavoriteAccount();
            fav.setUserId(req.getUserId());
            fav.setFavoriteUserId(req.getFavoriteUserId());
            favoriteAccountRepository.save(fav);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "新增常用帳號失敗"));
        }
    }
    
    @DeleteMapping("/favorite-accounts")
    public ResponseEntity<?> deleteFavorite(@RequestParam String userId, @RequestParam String favoriteUserId) {
        try {
            // Need transactional for delete
            // Better to move to service, but for quick impl:
            // favoriteAccountRepository.deleteByUserIdAndFavoriteUserId(userId, favoriteUserId);
            // JPA delete requires transaction usually.
            // Let's do it manually or assume Service handles it if we move it there.
            // For now, let's just find and delete.
            FavoriteAccount fav = favoriteAccountRepository.findById(new FavoriteAccountId(userId, favoriteUserId)).orElseThrow();
            favoriteAccountRepository.delete(fav);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "刪除常用帳號失敗"));
        }
    }
    
    @GetMapping("/favorite-accounts/{userId}")
    public ResponseEntity<?> getFavorites(@PathVariable String userId) {
        List<FavoriteAccount> favs = favoriteAccountRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (FavoriteAccount fav : favs) {
            userRepository.findById(fav.getFavoriteUserId()).ifPresent(u -> {
                result.add(Map.of(
                    "id", u.getId(),
                    "accountNumber", u.getId(),
                    "realName", u.getRealName()
                ));
            });
        }
        return ResponseEntity.ok(result);
    }
    
    // Admin APIs (Simplified for brevity, can be expanded)
    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findByRoleNot("admin");
        // Enrich with balance...
        // For now just return users
        return ResponseEntity.ok(users);
    }
}
