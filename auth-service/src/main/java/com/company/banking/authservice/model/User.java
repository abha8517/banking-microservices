package com.company.banking.authservice.model;

import jakarta.persistence.*;
import java.util.Set;
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;
    // getters/setters omitted for brevity
}
