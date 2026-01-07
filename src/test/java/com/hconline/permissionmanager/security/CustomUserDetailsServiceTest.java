package com.hconline.permissionmanager.security;

import com.hconline.permissionmanager.entity.Permission;
import com.hconline.permissionmanager.entity.Role;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
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
    void cargarUsuarioConMultiplesRoles_devuelveUserDetailsConTodosLosPermisos() {
        // Arrange
        Permission readUser = new Permission();
        readUser.setId(1L);
        readUser.setName("READ_USER");
        readUser.setDescription("Leer usuarios");

        Permission updateUser = new Permission();
        updateUser.setId(2L);
        updateUser.setName("UPDATE_USER");
        updateUser.setDescription("Actualizar usuarios");

        Permission readAudit = new Permission();
        readAudit.setId(3L);
        readAudit.setName("READ_AUDIT");
        readAudit.setDescription("Leer auditoría");

        Role roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setName("USER");
        roleUser.setPermissions(Set.of(readUser));

        Role roleSupport = new Role();
        roleSupport.setId(2L);
        roleSupport.setName("SUPPORT");
        roleSupport.setPermissions(Set.of(readUser, readAudit));

        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEnabled(true);
        user.setRoles(Set.of(roleUser, roleSupport));

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@email.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@email.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());

        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(authorities.contains("READ_USER"));
        assertTrue(authorities.contains("READ_AUDIT"));
        assertEquals(2, authorities.size()); // READ_USER y READ_AUDIT (sin duplicados)

        verify(userRepository, times(1)).findByEmail("test@email.com");
    }

    @Test
    void cargarUsuarioNoEncontrado_lanzaUsernameNotFoundException() {
        // Arrange
        when(userRepository.findByEmail("noexiste@email.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("noexiste@email.com");
        });

        verify(userRepository, times(1)).findByEmail("noexiste@email.com");
    }

    @Test
    void permisosExtractadosCorrectamente_sinDuplicados() {
        // Arrange
        Permission readUser = new Permission();
        readUser.setId(1L);
        readUser.setName("READ_USER");
        readUser.setDescription("Leer usuarios");

        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ROLE1");
        role1.setPermissions(Set.of(readUser));

        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("ROLE2");
        role2.setPermissions(Set.of(readUser)); // Mismo permiso

        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEnabled(true);
        user.setRoles(Set.of(role1, role2));

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@email.com");

        // Assert
        assertEquals(1, userDetails.getAuthorities().size()); // Solo 1 permiso, sin duplicados
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("READ_USER")));
    }

    @Test
    void usuarioSinRoles_devuelveUserDetailsSinPermisos() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEnabled(true);
        user.setRoles(new HashSet<>());

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@email.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals(0, userDetails.getAuthorities().size());
    }

    @Test
    void usuarioDeshabilitado_devuelveUserDetailsDeshabilitado() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@email.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEnabled(false);
        user.setRoles(new HashSet<>());

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@email.com");

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void rolesConVariosPermisos_todosLosPermisosSeExtraenCorrectamente() {
        // Arrange
        Permission p1 = new Permission();
        p1.setId(1L);
        p1.setName("PERM1");
        p1.setDescription("Permiso 1");

        Permission p2 = new Permission();
        p2.setId(2L);
        p2.setName("PERM2");
        p2.setDescription("Permiso 2");

        Permission p3 = new Permission();
        p3.setId(3L);
        p3.setName("PERM3");
        p3.setDescription("Permiso 3");

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");
        adminRole.setPermissions(Set.of(p1, p2, p3));

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@email.com");
        user.setUsername("admin");
        user.setPassword("encodedPassword");
        user.setEnabled(true);
        user.setRoles(Set.of(adminRole));

        when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@email.com");

        // Assert
        assertEquals(3, userDetails.getAuthorities().size());
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(authorities.contains("PERM1"));
        assertTrue(authorities.contains("PERM2"));
        assertTrue(authorities.contains("PERM3"));
    }
}
