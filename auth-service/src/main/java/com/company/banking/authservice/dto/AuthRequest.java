package com.company.banking.authservice.dto;

public record AuthRequest(
    String username,
    String password
) {}
