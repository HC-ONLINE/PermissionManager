package com.hconline.permissionmanager.controller;

import com.hconline.permissionmanager.dto.UserResponse;
import com.hconline.permissionmanager.service.UserService;
import com.hconline.permissionmanager.security.CustomUserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.hconline.permissionmanager.config.SecurityConfig;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Set;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("Debe obtener usuario si tiene permiso READ_USER y es admin")
    void getUser_asAdmin() throws Exception {
        UserResponse response = new UserResponse(1L, "user1", "user1@example.com", Set.of("USER"));
        Mockito.when(userService.getUserById(1L)).thenReturn(response);

        CustomUserPrincipal principal = new CustomUserPrincipal(
                1L,
                "admin",
                "password",
                Set.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("READ_USER")));

        mockMvc.perform(get("/users/1").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    @DisplayName("Debe obtener usuario si es el mismo usuario autenticado")
    void getUser_asSelf() throws Exception {
        UserResponse response = new UserResponse(1L, "user1", "user1@example.com", Set.of("USER"));
        Mockito.when(userService.getUserById(1L)).thenReturn(response);

        CustomUserPrincipal principal = new CustomUserPrincipal(
                1L,
                "user1",
                "password",
                Set.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("READ_USER")));

        mockMvc.perform(get("/users/1").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"));
    }

    @Test
    @DisplayName("Debe devolver 403 si no es admin ni el mismo usuario")
    void getUser_forbidden() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                1L,
                "user1",
                "password",
                Set.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("READ_USER")));

        mockMvc.perform(get("/users/2").with(user(principal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Debe eliminar usuario si es admin y tiene permiso DELETE_USER")
    void deleteUser_asAdmin() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                2L,
                "admin",
                "password",
                Set.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("DELETE_USER")));

        mockMvc.perform(delete("/users/1").with(user(principal)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Debe devolver 403 si no es admin al eliminar usuario")
    void deleteUser_forbidden() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                1L,
                "user1",
                "password",
                Set.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("DELETE_USER")));

        mockMvc.perform(delete("/users/1").with(user(principal)))
                .andExpect(status().isForbidden());
    }
}
