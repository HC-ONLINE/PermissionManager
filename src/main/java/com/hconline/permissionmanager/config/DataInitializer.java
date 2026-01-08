package com.hconline.permissionmanager.config;

import com.hconline.permissionmanager.entity.Permission;
import com.hconline.permissionmanager.entity.Role;
import com.hconline.permissionmanager.entity.User;
import com.hconline.permissionmanager.repository.PermissionRepository;
import com.hconline.permissionmanager.repository.RoleRepository;
import com.hconline.permissionmanager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DataInitializer(UserRepository userRepository, 
                          RoleRepository roleRepository,
                          PermissionRepository permissionRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void run(String... args) {
        Permission readUser = permissionRepository.save(new Permission("READ_USER"));
        Permission updateUser = permissionRepository.save(new Permission("UPDATE_USER"));
        Permission deleteUser = permissionRepository.save(new Permission("DELETE_USER"));
        
        Role userRole = new Role("USER");
        userRole.setPermissions(Set.of(readUser));
        userRole = roleRepository.save(userRole);
        
        Role adminRole = new Role("ADMIN");
        adminRole.setPermissions(Set.of(readUser, updateUser, deleteUser));
        adminRole = roleRepository.save(adminRole);
        
        Role supportRole = new Role("SUPPORT");
        supportRole.setPermissions(Set.of(readUser));
        supportRole = roleRepository.save(supportRole);
        
        User user1 = new User("user1", passwordEncoder.encode("password"), "user1@example.com");
        user1.setRoles(Set.of(userRole));
        userRepository.save(user1);
        
        User admin = new User("admin", passwordEncoder.encode("admin"), "admin@example.com");
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);
        
        User support = new User("support", passwordEncoder.encode("support"), "support@example.com");
        support.setRoles(Set.of(supportRole));
        userRepository.save(support);
    }
}
