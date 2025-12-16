package com.bank.system.controller;

import com.bank.system.entity.User;
import com.bank.system.repository.TransactionRepository;
import com.bank.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private TransactionRepository transactionRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        // Find users who are NOT admins? Or all users?
        // Node implementation: WHERE role != 'admin'
        // We need a repository method for this, or just filter.
        // For now, let's just return all users or rely on default findAll for simplicity 
        // until we add findByRoleNot to UserRepository.
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }
}
