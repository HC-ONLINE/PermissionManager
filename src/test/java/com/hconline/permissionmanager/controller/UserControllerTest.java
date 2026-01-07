package com.hconline.permissionmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hconline.permissionmanager.config.TestDataConfig;
import com.hconline.permissionmanager.dto.UpdateUserRequest;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestDataConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestDataConfig.TestDataInitializer testDataInitializer;

    @BeforeEach
    void setUp() {
        testDataInitializer.initializeTestData();
    }

    @Test
    @WithMockUser(username = "admin@email.com", authorities = { "READ_USER", "DELETE_USER" })
    void obtenerUsuarioExistente_devuelve200() throws Exception {
        User user = userRepository.findByEmail("user@email.com").orElseThrow();

        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("user@email.com"))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.permissions").isArray());
    }

    @Test
    @WithMockUser(username = "admin@email.com", authorities = { "READ_USER", "DELETE_USER" })
    void obtenerUsuarioInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/users/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "admin@email.com", authorities = { "UPDATE_USER", "DELETE_USER" })
    void actualizarUsuario_devuelve200() throws Exception {
        User user = userRepository.findByEmail("user@email.com").orElseThrow();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("user_updated@email.com");

        mockMvc.perform(put("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user_updated@email.com"));
    }

    @Test
    @WithMockUser(username = "admin@email.com", authorities = { "UPDATE_USER", "DELETE_USER" })
    void actualizarUsuarioInexistente_devuelve404() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("nuevo@email.com");

        mockMvc.perform(put("/users/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@email.com", authorities = "DELETE_USER")
    void eliminarUsuario_devuelve204() throws Exception {
        User support = userRepository.findByEmail("support@email.com").orElseThrow();

        mockMvc.perform(delete("/users/" + support.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@email.com", authorities = "READ_USER")
    void eliminarUsuarioSinPermiso_devuelve403() throws Exception {
        User admin = userRepository.findByEmail("admin@email.com").orElseThrow();

        mockMvc.perform(delete("/users/" + admin.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@email.com", authorities = { "READ_USER", "UPDATE_USER" })
    void usuarioActualizaSuPropioPerfil_devuelve200() throws Exception {
        User user = userRepository.findByEmail("user@email.com").orElseThrow();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("my_new_email@email.com");

        mockMvc.perform(put("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("my_new_email@email.com"));
    }

    @Test
    @WithMockUser(username = "user@email.com", authorities = "READ_USER")
    void usuarioIntentaActualizarOtroPerfil_devuelve403() throws Exception {
        User admin = userRepository.findByEmail("admin@email.com").orElseThrow();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("hacked@email.com");

        mockMvc.perform(put("/users/" + admin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void accederSinAutenticacion_devuelve401() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@email.com", authorities = { "UPDATE_USER", "DELETE_USER" })
    void actualizarConEmailInvalido_devuelve400() throws Exception {
        User user = userRepository.findByEmail("user@email.com").orElseThrow();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("notanemail");

        mockMvc.perform(put("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
