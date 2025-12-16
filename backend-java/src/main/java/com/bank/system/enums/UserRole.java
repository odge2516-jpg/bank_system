package com.bank.system.enums;

public enum UserRole {
    user,
    admin;

    // Helper to handle case-insensitive parsing if needed, 
    // though Spring Data/Jackson usually handles logical mapping well.
}
