package com.company.banking.authservice.service;

import com.company.banking.authservice.dto.AuthRequest;
import com.company.banking.authservice.dto.AuthResponse;
import com.company.banking.authservice.dto.RegisterRequest;
import com.company.banking.authservice.model.User;
import com.company.banking.authservice.repository.UserRepository;
import com.company.banking.authservice.util.JwtUtil;
import com.company.banking.grpc.person.CreatePersonRequest;
import com.company.banking.grpc.person.PersonResponse;
import com.company.banking.grpc.person.PersonServiceGrpc;
import com.company.banking.grpc.notification.NotificationRequest;
import com.company.banking.grpc.notification.NotificationServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PersonServiceGrpc.PersonServiceBlockingStub personServiceBlockingStub;
    private final NotificationServiceGrpc.NotificationServiceBlockingStub notificationServiceBlockingStub;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       @GrpcClient("person-service") PersonServiceGrpc.PersonServiceBlockingStub personServiceBlockingStub,
                       @GrpcClient("notification-service") NotificationServiceGrpc.NotificationServiceBlockingStub notificationServiceBlockingStub) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.personServiceBlockingStub = personServiceBlockingStub;
        this.notificationServiceBlockingStub = notificationServiceBlockingStub;
    }

    @Transactional
    public void register(RegisterRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(u -> {
            throw new IllegalStateException("Username already taken");
        });

        CreatePersonRequest personRequest = CreatePersonRequest.newBuilder()
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setEmail(request.email())
                .setPhone(request.phone())
                .build();
        PersonResponse newPerson = personServiceBlockingStub.createPerson(personRequest);

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPersonId(newPerson.getId());
        user.setRoles(Set.of("ROLE_USER"));
        userRepository.save(user);

        NotificationRequest notificationRequest = NotificationRequest.newBuilder()
                .setTo(newPerson.getEmail())
                .setSubject("Welcome to Our Bank!")
                .setBody("Hello " + newPerson.getFirstName() + ", thank you for registering.")
                .build();
        notificationServiceBlockingStub.sendNotification(notificationRequest);
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
