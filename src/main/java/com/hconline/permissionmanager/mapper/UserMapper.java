package com.hconline.permissionmanager.mapper;

import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.entity.Role;
import com.hconline.permissionmanager.entity.Permission;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    // DTO básico para respuesta de usuario
    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
    }

    // DTO extendido con permisos
    public static UserResponseWithPermissions toResponseWithPermissions(User user) {
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
        return new UserResponseWithPermissions(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                permissions);
    }

    // DTOs internos
    public record UserResponse(Long id, String username, String email, boolean enabled, Set<String> roles) {
    }

    public record UserResponseWithPermissions(Long id, String username, String email, boolean enabled,
            Set<String> roles, Set<String> permissions) {
    }
}