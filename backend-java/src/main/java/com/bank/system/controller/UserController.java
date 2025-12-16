package com.bank.system.controller;

import com.bank.system.entity.FavoriteAccount;
import com.bank.system.entity.SubAccount;
import com.bank.system.entity.User;
import com.bank.system.repository.FavoriteAccountRepository;
import com.bank.system.repository.SubAccountRepository;
import com.bank.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private SubAccountRepository subAccountRepository;
    @Autowired private FavoriteAccountRepository favoriteAccountRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        
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
    }
    
    // Admin endpoints could also go here or in AdminController
    // For simplicity, I'll keep them here or assume they are handled similarly
}
