package com.bank.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "favorite_accounts")
@IdClass(FavoriteAccountId.class)
public class FavoriteAccount {
    @Id
    @Column(name = "user_id", length = 12)
    private String userId;

    @Id
    @Column(name = "favorite_user_id", length = 12)
    private String favoriteUserId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}


