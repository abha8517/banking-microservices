package com.company.banking.notificationservice.dto;

public record NotificationRequest(
    String to,
    String subject,
    String body
) {}
