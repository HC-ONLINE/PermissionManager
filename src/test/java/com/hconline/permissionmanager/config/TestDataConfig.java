package com.hconline.permissionmanager.config;

import com.hconline.permissionmanager.entity.Permission;
import com.hconline.permissionmanager.entity.Role;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.PermissionRepository;
import com.hconline.permissionmanager.repository.RoleRepository;
import com.hconline.permissionmanager.repository.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@TestConfiguration
public class TestDataConfig {

    @Bean
    public TestDataInitializer testDataInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder) {
        return new TestDataInitializer(userRepository, roleRepository, permissionRepository, passwordEncoder);
    }

    public static class TestDataInitializer {
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PermissionRepository permissionRepository;
        private final PasswordEncoder passwordEncoder;

        public TestDataInitializer(UserRepository userRepository,
                RoleRepository roleRepository,
                PermissionRepository permissionRepository,
                PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.roleRepository = roleRepository;
            this.permissionRepository = permissionRepository;
            this.passwordEncoder = passwordEncoder;
        }

        public void initializeTestData() {
            // Buscar o crear permisos (idempotente - tolerante a ejecución paralela)
            Permission readUser = findOrCreatePermission("READ_USER", "Leer usuarios");
            Permission createUser = findOrCreatePermission("CREATE_USER", "Crear usuarios");
            Permission updateUser = findOrCreatePermission("UPDATE_USER", "Actualizar usuarios");
            Permission deleteUser = findOrCreatePermission("DELETE_USER", "Eliminar usuarios");
            Permission readAudit = findOrCreatePermission("READ_AUDIT", "Leer auditoría");

            // Buscar o crear rol USER
            Role userRole = findOrCreateRole("USER", "Usuario estándar", new HashSet<>(Set.of(readUser)));

            // Buscar o crear rol ADMIN
            Role adminRole = findOrCreateRole("ADMIN", "Administrador",
                    new HashSet<>(Set.of(readUser, createUser, updateUser, deleteUser, readAudit)));

            // Buscar o crear rol SUPPORT
            Role supportRole = findOrCreateRole("SUPPORT", "Soporte",
                    new HashSet<>(Set.of(readUser, readAudit)));

            // Buscar o crear usuario USER
            findOrCreateUser("user@email.com", "user123", "user", new HashSet<>(Set.of(userRole)));

            // Buscar o crear usuario ADMIN
            findOrCreateUser("admin@email.com", "admin123", "admin", new HashSet<>(Set.of(adminRole)));

            // Buscar o crear usuario SUPPORT
            findOrCreateUser("support@email.com", "support123", "support", new HashSet<>(Set.of(supportRole)));
        }

        private Permission findOrCreatePermission(String name, String description) {
            return permissionRepository.findByName(name).orElseGet(() -> {
                Permission permission = new Permission();
                permission.setName(name);
                permission.setDescription(description);
                return permissionRepository.save(permission);
            });
        }

        private Role findOrCreateRole(String name, String description, Set<Permission> permissions) {
            return roleRepository.findByName(name).orElseGet(() -> {
                Role role = new Role();
                role.setName(name);
                role.setDescription(description);
                role.setPermissions(permissions);
                return roleRepository.save(role);
            });
        }

        private User findOrCreateUser(String email, String password, String username, Set<Role> roles) {
            return userRepository.findByEmail(email).orElseGet(() -> {
                User user = new User();
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                user.setUsername(username);
                user.setEnabled(true);
                user.setRoles(roles);
                return userRepository.save(user);
            });
        }
    }
}
