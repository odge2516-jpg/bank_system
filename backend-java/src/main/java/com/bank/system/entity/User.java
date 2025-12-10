package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(length = 12)
    private String id;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "real_name", nullable = false, length = 100)
    private String realName;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "ENUM('user', 'admin') DEFAULT 'user'")
    private String role;

    @Column(columnDefinition = "ENUM('active', 'frozen') DEFAULT 'active'")
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SubAccount> subAccounts;
}
