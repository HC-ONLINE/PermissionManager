package com.hconline.permissionmanager.security;

import com.hconline.permissionmanager.entity.Permission;
import com.hconline.permissionmanager.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserPrincipalTest {
    @Test
    @DisplayName("Debe exponer datos correctamente")
    void principalData() {
        Role role = new Role("ADMIN");
        Permission perm = new Permission("READ_USER");
        CustomUserPrincipal principal = new CustomUserPrincipal(1L, "admin", "pass",
                Set.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("READ_USER")));
        assertEquals(1L, principal.getId());
        assertEquals("admin", principal.getUsername());
        assertEquals("pass", principal.getPassword());
        assertTrue(principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
        assertTrue(principal.isEnabled());
    }
}
