package com.hconline.permissionmanager.controller;

import com.hconline.permissionmanager.dto.LoginRequest;
import com.hconline.permissionmanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
