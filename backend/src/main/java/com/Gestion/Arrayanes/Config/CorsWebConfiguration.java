package com.Gestion.Arrayanes.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS centralizada para toda la API.
 * El frontend compilado se sirve desde el mismo origen, por lo que CORS solo
 * se necesita para el servidor de desarrollo de Angular (puerto 4200).
 */
@Configuration
public class CorsWebConfiguration implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsWebConfiguration(
            @Value("${arrayanes.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
            String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
