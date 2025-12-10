package com.bank.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteAccountId implements Serializable {
    private String userId;
    private String favoriteUserId;
}
