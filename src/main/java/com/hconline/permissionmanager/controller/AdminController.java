package com.hconline.permissionmanager.controller;

import com.hconline.permissionmanager.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    /**
     * Obtener logs de auditoría del sistema.
     * Requiere permiso READ_AUDIT.
     * Solo accesible por usuarios con rol ADMIN o SUPPORT.
     */
    @GetMapping("/audit")
    @PreAuthorize("hasAuthority('READ_AUDIT')")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs() {
        // Mock inicial de logs de auditoría
        List<AuditLogResponse> mockLogs = List.of(
                new AuditLogResponse(
                        1L,
                        "LOGIN",
                        "admin",
                        "Usuario admin inició sesión exitosamente",
                        LocalDateTime.now().minusHours(2)
                ),
                new AuditLogResponse(
                        2L,
                        "UPDATE_USER",
                        "admin",
                        "Usuario admin actualizó perfil de usuario ID: 2",
                        LocalDateTime.now().minusHours(1)
                ),
                new AuditLogResponse(
                        3L,
                        "DELETE_USER",
                        "admin",
                        "Usuario admin eliminó usuario ID: 3",
                        LocalDateTime.now().minusMinutes(30)
                ),
                new AuditLogResponse(
                        4L,
                        "LOGIN",
                        "support",
                        "Usuario support inició sesión exitosamente",
                        LocalDateTime.now().minusMinutes(15)
                ),
                new AuditLogResponse(
                        5L,
                        "READ_USER",
                        "support",
                        "Usuario support consultó perfil de usuario ID: 1",
                        LocalDateTime.now().minusMinutes(5)
                )
        );

        return ResponseEntity.ok(mockLogs);
    }
}
