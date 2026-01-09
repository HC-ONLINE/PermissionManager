package com.hconline.permissionmanager.service;

import com.hconline.permissionmanager.dto.UpdateUserRequest;
import com.hconline.permissionmanager.dto.UserResponse;
import com.hconline.permissionmanager.entity.Role;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe obtener usuario por ID")
    void getUserById() {
        User user = new User("user1", "pass", "mail");
        user.setId(1L);
        user.setRoles(Set.of(new Role("USER")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserResponse response = userService.getUserById(1L);
        assertEquals("user1", response.getUsername());
    }

    @Test
    @DisplayName("Debe lanzar excepción si usuario no existe")
    void getUserById_notFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.getUserById(1L));
    }

    @Test
    @DisplayName("Debe actualizar email y password")
    void updateUser() {
        User user = new User("user1", "oldpass", "old@mail");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        UpdateUserRequest req = new UpdateUserRequest("new@mail", "newpass");
        UserResponse response = userService.updateUser(1L, req);
        assertEquals("new@mail", response.getEmail());
    }

    @Test
    @DisplayName("Debe eliminar usuario existente")
    void deleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        assertDoesNotThrow(() -> userService.deleteUser(1L));
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar usuario inexistente")
    void deleteUser_notFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
    }
}
