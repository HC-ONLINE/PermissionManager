package com.hconline.permissionmanager.service;

import com.hconline.permissionmanager.dto.UpdateUserRequest;
import com.hconline.permissionmanager.dto.UserResponse;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.RoleRepository;
import com.hconline.permissionmanager.repository.UserRepository;
import com.hconline.permissionmanager.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserById_usuarioExiste_devuelveUserResponse() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("test@email.com");
        UserResponse response = userService.getUserById(1L, authentication);
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void getUserById_usuarioNoExiste_lanzaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L, authentication));
    }

    @Test
    void updateUser_usuarioNoExiste_lanzaResourceNotFoundException() {
        UpdateUserRequest req = new UpdateUserRequest();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(99L, req, authentication));
    }

    @Test
    void updateUser_sinPermiso_lanzaAccessDeniedException() {
        User user = new User();
        user.setId(1L);
        user.setEmail("otro@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("distinto@email.com");
        // Simular que no tiene autoridad
        when(authentication.getAuthorities()).thenReturn(Set.of());
        UpdateUserRequest req = new UpdateUserRequest();
        assertThrows(AccessDeniedException.class, () -> userService.updateUser(1L, req, authentication));
    }

    @Test
    void deleteUser_usuarioNoExiste_lanzaResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
    }
}
