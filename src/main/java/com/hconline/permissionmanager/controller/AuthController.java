package com.hconline.permissionmanager.controller;

import com.hconline.permissionmanager.dto.LoginRequest;
import com.hconline.permissionmanager.dto.ErrorResponse;
import com.hconline.permissionmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthService.LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginRequest serviceRequest = new AuthService.LoginRequest(request.getEmail(),
                request.getPassword());
        AuthService.LoginResponse response = authService.login(serviceRequest);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse("Credenciales inválidas", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
