package com.sewagealert.notification.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sewagealert.notification.exception.NotificationProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

// JsonUtils: Central JSON (de)serialization for the metadata column.
// Uses a single shared ObjectMapper to avoid allocating mappers per call (high-throughput friendly).
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private JsonUtils() {}

    // toJson: Serializes a metadata map to a JSON string (null-safe)
    public static String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(metadata);
        } catch (Exception ex) {
            log.error("Failed to serialize metadata to JSON", ex);
            throw new NotificationProcessingException("Failed to serialize notification metadata");
        }
    }

    // fromJson: Deserializes a JSON string into a metadata map (null/blank-safe)
    public static Map<String, Object> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception ex) {
            log.error("Failed to deserialize metadata from JSON: {}", json, ex);
            return new HashMap<>();
        }
    }
}
