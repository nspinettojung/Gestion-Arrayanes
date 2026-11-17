package com.Gestion.Arrayanes.Launcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * Token efímero que autoriza el apagado local. Se genera en cada arranque; la
 * propiedad arrayanes.shutdown.token permite fijarlo en tests.
 */
@Component
public class ShutdownToken {

    private final String value;

    public ShutdownToken(@Value("${arrayanes.shutdown.token:}") String configured) {
        this.value = configured.isBlank() ? UUID.randomUUID().toString() : configured;
    }

    public String value() {
        return value;
    }

    public boolean matches(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                value.getBytes(StandardCharsets.UTF_8), candidate.getBytes(StandardCharsets.UTF_8));
    }
}
