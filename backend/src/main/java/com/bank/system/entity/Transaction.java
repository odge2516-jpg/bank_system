package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @Column(length = 50)
    private String id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false)
    private Long amount;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false, length = 50)
    private String time;

    @Column(nullable = false)
    private Long timestamp;

    @Column(name = "sub_account_id", length = 50)
    private String subAccountId;
}
