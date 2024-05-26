package com.backend.rest.auth;

import com.backend.configuration.JwtService;
import com.backend.rest.ResponseGenerator;
import com.backend.rest.auth.dto.AuthenticationResponse;
import com.backend.rest.auth.dto.LoginRequest;
import com.backend.rest.auth.dto.RegisterRequest;
import com.backend.rest.auth.exc.DuplicateUsernameExc;
import com.backend.rest.user.User;
import com.backend.rest.user.UserService;
import com.backend.rest.user.dto.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) throws DuplicateUsernameExc {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(service.authenticate(request));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleException(DuplicateUsernameExc exc) {
        return new ResponseEntity<>(ResponseGenerator.createFromExc(exc), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleException(AuthenticationException exc) {
        return new ResponseEntity<>(ResponseGenerator.createFromExc(exc), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleException(Exception exc) {
        return new ResponseEntity<>(ResponseGenerator.createFromExc(exc), HttpStatus.BAD_REQUEST);
    }



}
