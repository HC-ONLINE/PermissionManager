package com.hconline.permissionmanager.service;

import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.UserRepository;
import com.hconline.permissionmanager.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_exitoso_devuelveTokenYUsuario() {
        AuthService.LoginRequest request = new AuthService.LoginRequest("test@email.com", "password");
        User user = new User();
        user.setEmail("test@email.com");
        user.setUsername("testuser");
        // Crear un mock de UserDetails
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@email.com");
        // Mock de Authentication que retorna el principal correcto
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(userRepository.findByEmail("test@email.com")).thenReturn(java.util.Optional.of(user));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("token123");

        AuthService.LoginResponse response = authService.login(request);
        assertNotNull(response.token());
        assertNotNull(response.user());
        assertEquals("testuser", response.user().username());
    }

    @Test
    void login_credencialesInvalidas_lanzaBadCredentialsException() {
        AuthService.LoginRequest request = new AuthService.LoginRequest("bad@email.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_usuarioNoEncontrado_lanzaException() {
        AuthService.LoginRequest request = new AuthService.LoginRequest("noexiste@email.com", "pass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("noexiste@email.com")).thenReturn(java.util.Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.login(request));
    }
}
