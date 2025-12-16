package com.bank.system.controller;

import com.bank.system.dto.BankDTOs.FavoriteAccountRequest;
import com.bank.system.dto.BankDTOs.SubAccountRequest;
import com.bank.system.entity.FavoriteAccount;
import com.bank.system.entity.SubAccount;
import com.bank.system.repository.FavoriteAccountRepository;
import com.bank.system.repository.UserRepository;
import com.bank.system.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired private AccountService accountService;
    @Autowired private FavoriteAccountRepository favoriteAccountRepository;
    @Autowired private UserRepository userRepository;

    // Sub-accounts
    @PostMapping("/sub-accounts")
    public ResponseEntity<?> createSubAccount(@RequestBody SubAccountRequest req) {
        SubAccount sub = accountService.createSubAccount(req);
        return ResponseEntity.ok(Map.of("success", true, "subAccount", sub));
    }

    @DeleteMapping("/sub-accounts/{subAccountId}")
    public ResponseEntity<?> deleteSubAccount(@PathVariable String subAccountId, @RequestParam String userId) {
        accountService.deleteSubAccount(subAccountId, userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Favorite Accounts
    @PostMapping("/favorite-accounts")
    public ResponseEntity<?> addFavorite(@RequestBody FavoriteAccountRequest req) {
        FavoriteAccount fav = new FavoriteAccount();
        fav.setUserId(req.getUserId());
        fav.setFavoriteUserId(req.getFavoriteUserId());
        favoriteAccountRepository.save(fav);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/favorite-accounts")
    public ResponseEntity<?> deleteFavorite(@RequestParam String userId, @RequestParam String favoriteUserId) {
        accountService.deleteFavorite(userId, favoriteUserId);
        return ResponseEntity.ok(Map.of("success", true));
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
}
