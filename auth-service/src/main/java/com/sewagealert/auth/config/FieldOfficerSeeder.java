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
public class FieldOfficerSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FieldOfficerProperties fieldOfficerProperties;

    @Override
    public void run(String... args) {

        int created = 0;

        for (FieldOfficerProperties.Officer config : fieldOfficerProperties.getAccounts()) {

            if (config.getEmail() == null || config.getEmail().isBlank()) {
                log.warn("Skipping field officer with a blank email.");
                continue;
            }

            if (userRepository.existsByEmail(config.getEmail())) {
                log.info("Field officer account already exists ({}). Skipping.", config.getEmail());
                continue;
            }

            User officer = new User();

            officer.setName(config.getName());
            officer.setEmail(config.getEmail());
            officer.setPassword(passwordEncoder.encode(config.getPassword()));
            officer.setRole(Role.FIELD_OFFICER);

            userRepository.save(officer);
            created++;

            log.info("Field officer account created successfully ({}).", officer.getEmail());
        }

        if (created == 0 && fieldOfficerProperties.getAccounts().isEmpty()) {
            log.info("No default field officers configured. Skipping initialization.");
        }
    }
}
