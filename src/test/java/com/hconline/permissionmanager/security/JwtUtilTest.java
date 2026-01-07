package com.hconline.permissionmanager.security;

import com.hconline.permissionmanager.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Configurar el secret y expiration usando reflexión para tests
        ReflectionTestUtils.setField(jwtUtil, "secret", "estaesunaclavesecretaparaeljwtquetienemasde32caracteres");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24 horas

        userDetails = new User(
                "test@email.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("READ_USER"))
        );
    }

    @Test
    void generarToken_devuelveTokenNoVacio() {
        // Act
        String token = jwtUtil.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes separadas por puntos
    }

    @Test
    void validarTokenValido_devuelveTrue() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void extraerUsername_devuelveUsernameCorrect() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertEquals("test@email.com", extractedUsername);
    }

    @Test
    void tokenConUsuarioDiferente_validacionFalla() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = new User(
                "otro@email.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("READ_USER"))
        );

        // Act
        Boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void tokenInvalido_lanzaExcepcion() {
        // Arrange
        String invalidToken = "token.invalido.aqui";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtil.extractUsername(invalidToken);
        });
    }

    @Test
    void extraerExpiration_devuelveFechaFutura() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // La fecha de expiración debe ser futura
    }

    @Test
    void tokenExpirado_validacionFalla() {
        // Arrange - Crear JwtUtil con expiración de 1ms
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", "estaesunaclavesecretaparaeljwtquetienemasde32caracteres");
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", 1L); // 1 milisegundo

        String token = shortExpirationJwtUtil.generateToken(userDetails);

        // Act - Esperar a que expire
        try {
            Thread.sleep(10); // Esperar 10ms para asegurar que expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        assertThrows(ExpiredJwtException.class, () -> {
            shortExpirationJwtUtil.extractUsername(token);
        });
    }

    @Test
    void generarTokenConEmailYRol_funcionaCorrectamente() {
        // Act
        String token = jwtUtil.generateToken("admin@email.com", "ADMIN");

        // Assert
        assertNotNull(token);
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals("admin@email.com", extractedUsername);
    }

    @Test
    void tokenConSecretIncorrecto_validacionFalla() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Crear otro JwtUtil con diferente secret
        JwtUtil differentSecretJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentSecretJwtUtil, "secret", "otraclavesecretadiferenteparaeljwtconmasde32caracteres");
        ReflectionTestUtils.setField(differentSecretJwtUtil, "expiration", 86400000L);

        // Act & Assert
        assertThrows(SignatureException.class, () -> {
            differentSecretJwtUtil.extractUsername(token);
        });
    }

    @Test
    void validateToken_sinUserDetails_devuelveTrueParaTokenValido() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act
        Boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_sinUserDetails_devuelveFalseParaTokenInvalido() {
        // Arrange
        String invalidToken = "token.invalido.aqui";

        // Act
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void dosTokensDelMismoUsuario_sonDiferentes() throws InterruptedException {
        // Arrange & Act
        String token1 = jwtUtil.generateToken(userDetails);
        Thread.sleep(1000); // Pausa de 1 segundo para garantizar timestamp diferente
        String token2 = jwtUtil.generateToken(userDetails);

        // Assert
        assertNotEquals(token1, token2); // Los tokens deben ser diferentes debido al timestamp
    }

    @Test
    void tokenGenera_contieneFechaEmision() {
        // Arrange
        String token = jwtUtil.generateToken(userDetails);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);
        Date now = new Date();

        // Assert - La expiración debe ser aproximadamente 24 horas después de ahora
        long difference = expiration.getTime() - now.getTime();
        assertTrue(difference > 86300000L && difference <= 86400000L); // Entre 23h 58min y 24h
    }
}
