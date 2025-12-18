package com.bank.system.controller;

import com.bank.system.dto.BankDTOs.TransactionRequest;
import com.bank.system.entity.User;
import com.bank.system.repository.TransactionRepository;
import com.bank.system.repository.UserRepository;
import com.bank.system.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransactionController {

    @Autowired private TransactionService transactionService;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody TransactionRequest req,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        // IDOR protection: verify the authenticated user matches the request
        String authenticatedUserId = getUserIdFromPrincipal(userDetails);
        if (authenticatedUserId == null || !req.getUserId().equals(authenticatedUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "無權限執行此操作"));
        }
        transactionService.deposit(req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody TransactionRequest req,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        String authenticatedUserId = getUserIdFromPrincipal(userDetails);
        if (authenticatedUserId == null || !req.getUserId().equals(authenticatedUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "無權限執行此操作"));
        }
        transactionService.withdraw(req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransactionRequest req,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        String authenticatedUserId = getUserIdFromPrincipal(userDetails);
        if (authenticatedUserId == null || !req.getUserId().equals(authenticatedUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "無權限執行此操作"));
        }
        transactionService.transfer(req);
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    @PostMapping("/sub-accounts/transfer")
    public ResponseEntity<?> transferBetweenSubAccounts(@RequestBody TransactionRequest req,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        String authenticatedUserId = getUserIdFromPrincipal(userDetails);
        if (authenticatedUserId == null || !req.getUserId().equals(authenticatedUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "無權限執行此操作"));
        }
        transactionService.transferBetweenSubAccounts(req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<?> getTransactions(@PathVariable String userId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        // IDOR protection: users can only view their own transactions
        String authenticatedUserId = getUserIdFromPrincipal(userDetails);
        if (authenticatedUserId == null || !userId.equals(authenticatedUserId)) {
            return ResponseEntity.status(403).body(Map.of("error", "無權限存取此資源"));
        }
        return ResponseEntity.ok(transactionRepository.findByUserIdOrderByTimestampDesc(userId));
    }
    
    private String getUserIdFromPrincipal(UserDetails userDetails) {
        if (userDetails == null) return null;
        // The username in JWT is the loginId, look up the actual user ID
        String loginId = userDetails.getUsername();
        return userRepository.findByLoginId(loginId)
                .map(User::getId)
                .orElse(null);
    }
}
