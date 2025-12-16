package com.bank.system.controller;

import com.bank.system.dto.BankDTOs.LoginRequest;
import com.bank.system.dto.BankDTOs.RegisterRequest;
import com.bank.system.entity.FavoriteAccount;
import com.bank.system.entity.SubAccount;
import com.bank.system.entity.User;
import com.bank.system.repository.FavoriteAccountRepository;
import com.bank.system.repository.SubAccountRepository;
import com.bank.system.repository.UserRepository;
import com.bank.system.security.JwtTokenProvider;
import com.bank.system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;
    @Autowired private SubAccountRepository subAccountRepository;
    @Autowired private FavoriteAccountRepository favoriteAccountRepository;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        User user = authService.register(req);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getLoginId(), req.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        // Fetch User details for response
        User user = userRepository.findByLoginId(req.getLoginId()).orElseThrow();

        // Construct response
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

        return ResponseEntity.ok(Map.of("success", true, "token", jwt, "user", userMap));
    }
}
