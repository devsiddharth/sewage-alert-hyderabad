package com.sewagealert.notification.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sewagealert.notification.config.EmailJsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

// EmailJsClient: Thin REST client for the EmailJS send API — the ONLY component that
// talks to EmailJS. Template rendering stays in the EmailJS dashboard; this client only
// forwards the configured template id + dynamic parameters.
//
//   POST https://api.emailjs.com/api/v1.0/email/send
//   { service_id, template_id, user_id (public key), accessToken (private key),
//     template_params: { name, email, verification_code, ... } }
//
// The private key is read from configuration (environment) and sent as `accessToken`.
// It is never logged.
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailJsClient {

    private static final String SEND_URL = "https://api.emailjs.com/api/v1.0/email/send";

    private final EmailJsProperties properties;

    // Bounded HTTP timeouts: a hung EmailJS call must never stall the RabbitMQ consumer thread
    // that drives the whole notification pipeline.
    private final RestClient restClient = RestClient.builder()
            .requestFactory(requestFactory())
            .build();

    private static SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(15_000);
        return factory;
    }

    // send: Sends an email through the configured service + template. Throws on transport
    // or HTTP errors — callers decide how to handle failures (they must not propagate into
    // the user's account state).
    public void send(String templateId, Map<String, Object> templateParams) {
        EmailJsSendRequest request = new EmailJsSendRequest(
                properties.getServiceId(),
                templateId,
                properties.getPublicKey(),
                properties.getPrivateKey(),
                templateParams);

        restClient.post()
                .uri(SEND_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    // EmailJsSendRequest: Wire format of the EmailJS REST API. Jackson maps the snake_case
    // field names via @JsonProperty.
    record EmailJsSendRequest(
            @JsonProperty("service_id") String serviceId,
            @JsonProperty("template_id") String templateId,
            @JsonProperty("user_id") String userId,
            @JsonProperty("accessToken") String accessToken,
            @JsonProperty("template_params") Map<String, Object> templateParams) {
    }
}
