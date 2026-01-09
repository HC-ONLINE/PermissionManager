package com.hconline.permissionmanager.security;

import com.hconline.permissionmanager.entity.Permission;
import com.hconline.permissionmanager.entity.Role;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe cargar usuario y authorities")
    void loadUserByUsername() {
        Role role = new Role("ADMIN");
        role.setPermissions(Set.of(new Permission("READ_USER")));
        User user = new User("admin", "pass", "mail");
        user.setId(1L);
        user.setRoles(Set.of(role));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        UserDetails details = customUserDetailsService.loadUserByUsername("admin");
        assertEquals("admin", details.getUsername());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("READ_USER")));
    }

    @Test
    @DisplayName("Debe lanzar excepción si usuario no existe")
    void loadUserByUsername_notFound() {
        when(userRepository.findByUsername("nope")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("nope"));
    }
}
