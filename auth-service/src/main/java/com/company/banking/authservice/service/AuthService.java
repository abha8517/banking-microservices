package com.company.banking.authservice.service;

import com.company.banking.authservice.client.PersonServiceFeignClient;
import com.company.banking.authservice.dto.AuthRequest;
import com.company.banking.authservice.dto.AuthResponse;
import com.company.banking.authservice.dto.RegisterRequest;
import com.company.banking.authservice.model.User;
import com.company.banking.authservice.repository.UserRepository;
import com.company.banking.authservice.util.JwtUtil;
import com.company.common.dto.PersonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PersonServiceFeignClient personServiceFeignClient;
    private final UserDetailsService userDetailsService;

    @Transactional
    public void register(RegisterRequest request) {
        // Optional: Check if username or email already exists in auth-service
        userRepository.findByUsername(request.username()).ifPresent(u -> {
            throw new IllegalStateException("Username already taken");
        });

        // Step 1: Create Person in person-service
        // Note: person-service will throw an exception if email/phone is a duplicate
        PersonDTO personRequest = new PersonDTO(null, request.firstName(), request.lastName(), request.email(), request.phone());
        PersonDTO newPerson = personServiceFeignClient.createPerson(personRequest);

        // Step 2: Create User in auth-service
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPersonId(newPerson.id());
        user.setRoles(Set.of("ROLE_USER")); // Default role
        userRepository.save(user);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        final var userDetails = userDetailsService.loadUserByUsername(request.username());
        final String jwt = jwtUtil.generateToken(userDetails);
        return new AuthResponse(jwt);
    }
}
