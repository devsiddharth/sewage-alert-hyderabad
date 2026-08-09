package com.sewagealert.complaint.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * UploadProperties: Tuning knobs for complaint image uploads.
 * <p>
 * Bound from the {@code app.upload} prefix in application.yml. {@code maxFileSize} is the
 * per-file ceiling enforced by complaint validation (independent of Spring's servlet
 * multipart limit, which guards the raw HTTP layer).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    /** Maximum size of a single complaint image. */
    private DataSize maxFileSize = DataSize.ofMegabytes(10);
}
