package com.company.banking.transactionservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private String id;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Instant createdAt;
    private String idempotencyKey;
    // getters/setters omitted
}
