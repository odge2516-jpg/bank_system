package com.bank.system.controller;

import com.bank.system.dto.BankDTOs.TransactionRequest;
import com.bank.system.repository.TransactionRepository;
import com.bank.system.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransactionController {

    @Autowired private TransactionService transactionService;
    @Autowired private TransactionRepository transactionRepository;

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody TransactionRequest req) {
        transactionService.deposit(req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody TransactionRequest req) {
        transactionService.withdraw(req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransactionRequest req) {
        transactionService.transfer(req);
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    @PostMapping("/sub-accounts/transfer")
    public ResponseEntity<?> transferBetweenSubAccounts(@RequestBody TransactionRequest req) {
        transactionService.transferBetweenSubAccounts(req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<?> getTransactions(@PathVariable String userId) {
        return ResponseEntity.ok(transactionRepository.findByUserIdOrderByTimestampDesc(userId));
    }
}
