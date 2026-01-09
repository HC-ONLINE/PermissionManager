package com.hconline.permissionmanager.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {
    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Debe cargar PasswordEncoder en el contexto")
    void passwordEncoderBean() {
        PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
        assertNotNull(encoder);
        assertTrue(encoder.matches("clave", encoder.encode("clave")));
    }
}
