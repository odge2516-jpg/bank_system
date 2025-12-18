package com.bank.system.dto;

import lombok.Data;
import java.math.BigDecimal;

public class BankDTOs {
    @Data
    public static class RegisterRequest {
        private String realName;
        private String loginId;
        private String password;
        private BigDecimal initialDeposit;
    }

    @Data
    public static class LoginRequest {
        private String loginId;
        private String password;
    }

    @Data
    public static class TransactionRequest {
        private String userId;
        private BigDecimal amount;
        private String subAccountId;
        // For transfer
        private String recipientAccountNumber;
        private boolean saveAsFavorite;
        // For sub-account transfer
        private String fromSubAccountId;
        private String toSubAccountId;
    }

    @Data
    public static class SubAccountRequest {
        private String userId;
        private String name;
        private String color;
    }
    
    @Data
    public static class BalanceUpdateRequest {
        private BigDecimal newBalance;
    }
    
    @Data
    public static class FavoriteAccountRequest {
        private String userId;
        private String favoriteUserId;
    }
}
