package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sub_accounts")
public class SubAccount {
    @Id
    @Column(length = 50)
    private String id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(length = 7)
    private String color;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
