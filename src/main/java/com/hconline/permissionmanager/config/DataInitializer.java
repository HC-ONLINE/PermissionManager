package com.hconline.permissionmanager.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hconline.permissionmanager.entity.Permission;
import com.hconline.permissionmanager.entity.Role;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.PermissionRepository;
import com.hconline.permissionmanager.repository.RoleRepository;
import com.hconline.permissionmanager.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ===== PERMISSIONS =====
            Permission readUser = permissionRepository.findByName("READ_USER")
                    .orElseGet(() -> {
                        Permission p = new Permission();
                        p.setName("READ_USER");
                        p.setDescription("Read users");
                        return permissionRepository.save(p);
                    });

            Permission updateUser = permissionRepository.findByName("UPDATE_USER")
                    .orElseGet(() -> {
                        Permission p = new Permission();
                        p.setName("UPDATE_USER");
                        p.setDescription("Update users");
                        return permissionRepository.save(p);
                    });

            Permission deleteUser = permissionRepository.findByName("DELETE_USER")
                    .orElseGet(() -> {
                        Permission p = new Permission();
                        p.setName("DELETE_USER");
                        p.setDescription("Delete users");
                        return permissionRepository.save(p);
                    });

            Permission readAudit = permissionRepository.findByName("READ_AUDIT")
                    .orElseGet(() -> {
                        Permission p = new Permission();
                        p.setName("READ_AUDIT");
                        p.setDescription("Read audit logs");
                        return permissionRepository.save(p);
                    });

            // ===== ROLES =====
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("ADMIN");
                        r.setDescription("Administrator");
                        r.getPermissions().addAll(
                                Set.of(readUser, updateUser, deleteUser, readAudit));
                        return roleRepository.save(r);
                    });

            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("USER");
                        r.setDescription("Standard user");
                        r.getPermissions().add(readUser);
                        return roleRepository.save(r);
                    });

            Role supportRole = roleRepository.findByName("SUPPORT")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("SUPPORT");
                        r.setDescription("Support user");
                        r.getPermissions().addAll(
                                Set.of(readUser, readAudit));
                        return roleRepository.save(r);
                    });

            // ===== USERS =====
            createUserIfNotExists(
                    "admin1",
                    "admin@example.com",
                    "password",
                    adminRole,
                    userRepository,
                    passwordEncoder);

            createUserIfNotExists(
                    "user1",
                    "user@example.com",
                    "password",
                    userRole,
                    userRepository,
                    passwordEncoder);

            createUserIfNotExists(
                    "support1",
                    "support@example.com",
                    "password",
                    supportRole,
                    userRepository,
                    passwordEncoder);
        };
    }

    private void createUserIfNotExists(
            String username,
            String email,
            String rawPassword,
            Role role,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.getRoles().add(role);

        userRepository.save(user);
    }
}
