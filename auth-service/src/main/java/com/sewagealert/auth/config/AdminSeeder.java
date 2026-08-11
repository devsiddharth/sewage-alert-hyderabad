package com.sewagealert.auth.config;

import com.sewagealert.auth.model.Role;
import com.sewagealert.auth.model.User;
import com.sewagealert.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    @Override
    public void run(String... args) {

        if (userRepository.existsByEmail(adminProperties.getEmail())) {
            log.info("Administrator account already exists. Skipping initialization.");
            return;
        }

        User admin = new User();

        admin.setName(adminProperties.getName());
        admin.setEmail(adminProperties.getEmail());
        admin.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
        admin.setRole(Role.ADMIN);
        // Seeded staff accounts are pre-verified — there is no inbox behind these addresses.
        admin.setEmailVerified(true);

        userRepository.save(admin);

        log.info("Default administrator account created successfully.");
    }
}