package com.backend.rest.auth;

import com.backend.configuration.JwtService;
import com.backend.rest.auth.dto.AuthenticationResponse;
import com.backend.rest.auth.dto.LoginRequest;
import com.backend.rest.auth.dto.RegisterRequest;
import com.backend.rest.auth.exc.DuplicateUsernameExc;
import com.backend.rest.user.User;
import com.backend.rest.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) throws DuplicateUsernameExc {


        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .isOnline(false)
                .friendList(new ArrayList<>())
                .friendRequests(new ArrayList<>())
                .friendRequestsReceive(new ArrayList<>())
                .role(request.getRole())
                .build();

        try {
            repository.save(user);
        } catch (Exception exc) {
            if (exc.getMessage().startsWith("could not execute statement [ERROR: duplicate key value violates unique constraint \"_user_username_key\"")) {
                throw new DuplicateUsernameExc("username already exists");
            }
            throw exc;
        }
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = repository.findByUsername(request.getUsername())
                .orElseThrow();

        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .build();
    }
}
