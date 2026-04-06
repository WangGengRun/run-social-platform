package com.run.runsocialplatform.common.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 同时支持前端常见 ISO-8601（含 {@code Z} / 偏移量）与 {@code yyyy-MM-dd HH:mm:ss}。
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter LEGACY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getValueAsString();
        if (text == null || text.isBlank()) {
            return null;
        }
        text = text.trim();

        if (text.endsWith("Z")) {
            return Instant.parse(text).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // fall through
        }

        try {
            return LocalDateTime.parse(text, LEGACY);
        } catch (DateTimeParseException ignored) {
            // fall through
        }

        return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
