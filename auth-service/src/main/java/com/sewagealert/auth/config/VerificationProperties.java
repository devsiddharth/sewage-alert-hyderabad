package com.sewagealert.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * VerificationProperties: Configuration for the email verification token lifecycle.
 * <p>
 * Bound from the {@code app.verification} prefix in application.yml — follows the
 * project convention of {@code @ConfigurationProperties} instead of scattered {@code @Value}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.verification")
public class VerificationProperties {

    /** Lifetime of a verification link in minutes. */
    private long tokenTtlMinutes = 30;
}
