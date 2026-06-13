package com.tutorial.crud.config;

import com.tutorial.crud.entity.UserInfo;
import com.tutorial.crud.entity.UserRole;
import com.tutorial.crud.repository.RoleRepository;
import com.tutorial.crud.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        UserRole roleUser = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            UserRole r = new UserRole();
            r.setName("ROLE_USER");
            return roleRepository.save(r);
        });

        UserRole roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            UserRole r = new UserRole();
            r.setName("ROLE_ADMIN");
            return roleRepository.save(r);
        });

        if (userRepository.findByUsername("admin") == null) {
            UserInfo admin = new UserInfo();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.getRoles().add(roleAdmin);
            admin.getRoles().add(roleUser);

            userRepository.save(admin);
            System.out.println("[STARTUP] Default Admin account created (admin/admin123).");
        }
    }
}