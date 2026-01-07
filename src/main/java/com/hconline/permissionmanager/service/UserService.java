package com.hconline.permissionmanager.service;

import com.hconline.permissionmanager.dto.UpdateUserRequest;
import com.hconline.permissionmanager.dto.UserResponse;
import com.hconline.permissionmanager.entity.Role;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.exception.ResourceNotFoundException;
import com.hconline.permissionmanager.repository.RoleRepository;
import com.hconline.permissionmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserResponse getUserById(Long id, Authentication auth) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        String currentEmail = auth.getName();
        // Ownership y control de acceso:
        // 1. Puede ver su propio perfil siempre
        // 2. Para ver perfiles ajenos, debe tener permisos privilegiados (READ_AUDIT o DELETE_USER)
        //    Esto distingue entre USER normal (solo su perfil) vs SUPPORT/ADMIN (todos los perfiles)
        boolean isOwnProfile = user.getEmail().equals(currentEmail);
        boolean hasPrivilegedAccess = hasAuthority(auth, "READ_AUDIT") || hasAuthority(auth, "DELETE_USER");
        
        if (!isOwnProfile && !hasPrivilegedAccess) {
            throw new AccessDeniedException("No tienes permiso para ver este usuario");
        }
        
        // Mapear a DTO con roles y permisos
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName()).collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), roles, permissions);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest req, Authentication auth) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        String currentEmail = auth.getName();
        boolean isAdmin = hasAuthority(auth, "UPDATE_USER");
        // Ownership: solo el usuario o admin puede actualizar
        if (!isAdmin && !user.getEmail().equals(currentEmail)) {
            throw new AccessDeniedException("No tienes permiso para actualizar este usuario");
        }
        // Actualizar email si provisto
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            user.setEmail(req.getEmail());
        }
        // Solo admin puede cambiar roles
        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            if (!isAdmin) {
                throw new AccessDeniedException("Solo admin puede cambiar roles");
            }
            Set<Role> newRoles = req.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(newRoles);
        }
        userRepository.save(user);
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName()).collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), roles, permissions);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        // Validar que no sea el último admin
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
        if (isAdmin) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN")))
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("No se puede eliminar el último usuario admin");
            }
        }
        userRepository.delete(user);
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(authority) || a.equals("ROLE_" + authority));
    }
}
