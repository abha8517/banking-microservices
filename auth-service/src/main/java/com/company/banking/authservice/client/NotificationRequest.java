package com.company.banking.authservice.client;

public record NotificationRequest(String to, String subject, String body) {}
