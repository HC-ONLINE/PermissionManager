package com.hconline.permissionmanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "READ_AUDIT")
    void accesoAAuditLog_conPermisoREAD_AUDIT_devuelve200() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].action").exists())
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].details").exists())
                .andExpect(jsonPath("$[0].timestamp").exists());
    }

    @Test
    @WithMockUser(authorities = "READ_USER")
    void accesoAAuditLog_sinPermisoREAD_AUDIT_devuelve403() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accesoAAuditLog_sinAutenticacion_devuelve401() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "READ_AUDIT")
    void auditLogDevuelveDatos_verificarEstructura() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].action").value("LOGIN"))
                .andExpect(jsonPath("$[1].action").value("UPDATE_USER"))
                .andExpect(jsonPath("$[2].action").value("DELETE_USER"))
                .andExpect(jsonPath("$[3].action").value("LOGIN"))
                .andExpect(jsonPath("$[4].action").value("READ_USER"));
    }
}
