package com.sewagealert.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AppProperties: Application-level configuration shared across features.
 * <p>
 * Bound from the {@code app} prefix in application.yml. Currently holds the public
 * frontend origin used to build verification links inside emails.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** Public origin of the React frontend — used to build the email verification link. */
    private String frontendUrl = "http://localhost:5173";
}
