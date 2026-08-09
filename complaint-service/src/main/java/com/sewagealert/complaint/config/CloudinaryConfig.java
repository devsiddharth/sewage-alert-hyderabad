package com.sewagealert.complaint.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * CloudinaryConfig: Builds the {@link Cloudinary} client from environment-backed
 * configuration. Secrets are never hardcoded — they come from
 * {@code CLOUDINARY_CLOUD_NAME}, {@code CLOUDINARY_API_KEY} and {@code CLOUDINARY_API_SECRET}
 * (falling back to empty values in local dev, where uploads simply fail at runtime).
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }
}
