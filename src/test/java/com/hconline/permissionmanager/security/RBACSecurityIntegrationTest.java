package com.hconline.permissionmanager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hconline.permissionmanager.config.TestDataConfig;
import com.hconline.permissionmanager.dto.UpdateUserRequest;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.UserRepository;
import com.hconline.permissionmanager.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestDataConfig.class)
class RBACSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TestDataConfig.TestDataInitializer testDataInitializer;

    private String userToken;
    private String adminToken;
    private String supportToken;
    private Long userId;
    private Long adminId;
    private Long supportId;

    @BeforeEach
    void setUp() {
        // Inicializar datos de prueba
        testDataInitializer.initializeTestData();

        // Obtener usuarios de la base de datos
        User user = userRepository.findByEmail("user@email.com").orElseThrow();
        User admin = userRepository.findByEmail("admin@email.com").orElseThrow();
        User support = userRepository.findByEmail("support@email.com").orElseThrow();

        userId = user.getId();
        adminId = admin.getId();
        supportId = support.getId();

        // Generar tokens para cada usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername("user@email.com");
        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin@email.com");
        UserDetails supportDetails = userDetailsService.loadUserByUsername("support@email.com");

        userToken = jwtUtil.generateToken(userDetails);
        adminToken = jwtUtil.generateToken(adminDetails);
        supportToken = jwtUtil.generateToken(supportDetails);
    }

    // =============== USUARIO USER TESTS ===============

    @Test
    void userPuedeLeerSuPropioPerfil() throws Exception {
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("user@email.com"));
    }

    @Test
    void userNoPuedeLeerPerfilDeOtroUsuario() throws Exception {
        mockMvc.perform(get("/users/" + adminId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void userNoPuedeActualizarPerfilDeOtroUsuario() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("nuevo@email.com");

        mockMvc.perform(put("/users/" + adminId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userPuedeActualizarSuPropioPerfil() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("user_updated@email.com");

        mockMvc.perform(put("/users/" + userId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user_updated@email.com"));
    }

    @Test
    void userNoPuedeEliminarNingunUsuario() throws Exception {
        mockMvc.perform(delete("/users/" + supportId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void userNoPuedeAccederAAdminAudit() throws Exception {
        mockMvc.perform(get("/admin/audit")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // =============== USUARIO ADMIN TESTS ===============

    @Test
    void adminPuedeLeerCualquierPerfil() throws Exception {
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    void adminPuedeActualizarCualquierPerfil() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("user_updated_by_admin@email.com");

        mockMvc.perform(put("/users/" + userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void adminPuedeEliminarCualquierUsuario() throws Exception {
        mockMvc.perform(delete("/users/" + supportId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminPuedeAccederAAdminAudit() throws Exception {
        mockMvc.perform(get("/admin/audit")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // =============== USUARIO SUPPORT TESTS ===============

    @Test
    void supportPuedeLeerCualquierPerfil() throws Exception {
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + supportToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    void supportNoPuedeActualizarPerfiles() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("support_trying_update@email.com");

        mockMvc.perform(put("/users/" + userId)
                .header("Authorization", "Bearer " + supportToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void supportNoPuedeEliminarUsuarios() throws Exception {
        mockMvc.perform(delete("/users/" + userId)
                .header("Authorization", "Bearer " + supportToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void supportPuedeAccederAAdminAudit() throws Exception {
        mockMvc.perform(get("/admin/audit")
                .header("Authorization", "Bearer " + supportToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // =============== OWNERSHIP VALIDATION TESTS ===============

    @Test
    void userPuedeActualizarSuEmail() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new_user_email@email.com");

        mockMvc.perform(put("/users/" + userId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new_user_email@email.com"));
    }

    @Test
    void userNoPuedeAsignarseRolesDeAdmin() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRoleIds(Set.of(1L)); // ID del rol ADMIN

        mockMvc.perform(put("/users/" + userId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminPuedeCambiarRolesDeCualquierUsuario() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRoleIds(Set.of(2L)); // ID del rol USER

        mockMvc.perform(put("/users/" + supportId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // =============== AUTENTICACIÓN TESTS ===============

    @Test
    void requestSinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestConTokenInvalido_devuelve401() throws Exception {
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer tokeninvalido123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestConTokenExpirado_devuelve401() throws Exception {
        // Token que ya expiró (generado manualmente con fecha pasada)
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsImlhdCI6MTYwMDAwMDAwMCwiZXhwIjoxNjAwMDAwMDAxfQ.invalid";

        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
