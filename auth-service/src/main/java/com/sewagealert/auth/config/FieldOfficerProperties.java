package com.sewagealert.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FieldOfficerProperties: Seed configuration for the default field officer accounts.
 * Each entry in the list is created by FieldOfficerSeeder on startup (if missing).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.field-officers")
public class FieldOfficerProperties {

    private List<Officer> accounts = new ArrayList<>();

    @Getter
    @Setter
    public static class Officer {
        private String name;
        private String email;
        private String password;
    }
}
