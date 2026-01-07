package com.hconline.permissionmanager.controller;

import com.hconline.permissionmanager.dto.UpdateUserRequest;
import com.hconline.permissionmanager.dto.UserResponse;
import com.hconline.permissionmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Obtener información de un usuario por ID.
     * Requiere permiso READ_USER.
     * Un usuario puede ver su propio perfil, o cualquier perfil si tiene permiso DELETE_USER (admin).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_USER')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, Authentication authentication) {
        UserResponse response = userService.getUserById(id, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar información de un usuario.
     * Los usuarios autenticados pueden actualizar su propio perfil (email, username).
     * Solo usuarios con permiso UPDATE_USER pueden actualizar perfiles de otros usuarios.
     * Solo admin puede cambiar roles.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        UserResponse response = userService.updateUser(id, request, authentication);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un usuario.
     * Requiere permiso DELETE_USER (solo admin).
     * No se puede eliminar el último usuario admin.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
