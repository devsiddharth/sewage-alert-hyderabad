package com.sewagealert.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EmailJsProperties: Configuration for the EmailJS REST API integration used to send
 * transactional emails (verification, welcome, complaint updates).
 * <p>
 * Bound from the {@code emailjs} prefix in application.yml. All credentials are injected
 * from the environment — the private key must NEVER be committed to source control.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "emailjs")
public class EmailJsProperties {

    /** EmailJS service id (e.g. service_2mr35tu). */
    private String serviceId;

    /** Default template id — used for the email verification message (e.g. template_w4koj8i). */
    private String templateId;

    /** EmailJS public key (user_id in the REST API payload). */
    private String publicKey;

    /** EmailJS private key (accessToken in the REST API payload) — env var only. */
    private String privateKey;

    /** Optional template id for the post-verification welcome email. When blank, no
     *  welcome email is sent (the channel stays dormant until a template is configured). */
    private String welcomeTemplateId;
}
