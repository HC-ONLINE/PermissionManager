package com.hconline.permissionmanager.controller;

import com.hconline.permissionmanager.config.SecurityConfig;
import com.hconline.permissionmanager.security.CustomUserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("Debe permitir acceso a /admin/audit solo a admin")
    void auditAccess_admin() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                2L,
                "admin",
                "password",
                Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(get("/admin/audit").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Audit log access granted"));
    }

    @Test
    @DisplayName("Debe denegar acceso a /admin/audit a usuarios no admin")
    void auditAccess_forbidden() throws Exception {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                1L,
                "user1",
                "password",
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));

        mockMvc.perform(get("/admin/audit").with(user(principal)))
                .andExpect(status().isForbidden());
    }
}
