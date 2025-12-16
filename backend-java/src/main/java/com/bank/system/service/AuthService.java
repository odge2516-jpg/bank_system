package com.bank.system.service;

import com.bank.system.dto.BankDTOs.RegisterRequest;
import com.bank.system.entity.SubAccount;
import com.bank.system.entity.User;
import com.bank.system.enums.UserRole;
import com.bank.system.enums.UserStatus;
import com.bank.system.repository.SubAccountRepository;
import com.bank.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private SubAccountRepository subAccountRepository;
    @Autowired private PasswordEncoder passwordEncoder;

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
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(UserRole.user);
        user.setStatus(UserStatus.active);
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
}
