package com.Gestion.Arrayanes.Config;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Sirve archivos estáticos y cae en index.html para las rutas de Angular.
 * Las rutas de la API nunca deben devolver el index: si no existen, 404.
 */
public class SpaResourceResolver extends PathResourceResolver {

    private static final String API_PREFIX = "api/";

    private final Resource index;

    public SpaResourceResolver(Resource index) {
        this.index = index;
    }

    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
        Resource requested = location.createRelative(resourcePath);

        if (requested.exists() && requested.isReadable()) {
            return requested;
        }

        if (resourcePath.startsWith(API_PREFIX)) {
            return null;
        }

        return index.exists() ? index : null;
    }
}
