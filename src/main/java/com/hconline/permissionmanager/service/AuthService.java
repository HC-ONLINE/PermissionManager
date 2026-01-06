package com.hconline.permissionmanager.service;

import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.mapper.UserMapper;
import com.hconline.permissionmanager.mapper.UserMapper.UserResponseWithPermissions;
import com.hconline.permissionmanager.repository.UserRepository;
import com.hconline.permissionmanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        try {
            // Autenticar usuario
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            // Cargar UserDetails y entidad User
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado tras autenticación"));

            // Generar JWT
            String jwt = jwtUtil.generateToken(userDetails);

            // Mapear a DTO con permisos
            UserResponseWithPermissions userDto = UserMapper.toResponseWithPermissions(user);

            return new LoginResponse(jwt, userDto);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Credenciales inválidas", ex);
        }
    }

    // DTOs para request/response
    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(String token, UserResponseWithPermissions user) {
    }
}