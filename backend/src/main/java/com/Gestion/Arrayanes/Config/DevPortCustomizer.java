package com.Gestion.Arrayanes.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;

/**
 * En desarrollo web elige el primer puerto libre de una lista corta, para no
 * depender de un único puerto fijo. El jar de escritorio usa server.port=0
 * (puerto efímero publicado en api-port.txt), así que no comparte estos puertos.
 */
@Component
@Profile("webdev")
public class DevPortCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private final List<Integer> candidatePorts;

    @Autowired
    public DevPortCustomizer(
            @Value("${arrayanes.dev.ports:8081,8082,8083,8084,8085,8086,8087,8088,8089}") int[] ports) {
        this(Arrays.stream(ports).boxed().toList());
    }

    DevPortCustomizer(List<Integer> candidatePorts) {
        if (candidatePorts == null || candidatePorts.isEmpty()) {
            throw new IllegalArgumentException("La lista de puertos de desarrollo no puede estar vacía.");
        }
        if (candidatePorts.stream().anyMatch(port -> port < 1 || port > 65535)) {
            throw new IllegalArgumentException("Los puertos de desarrollo deben estar entre 1 y 65535.");
        }
        this.candidatePorts = List.copyOf(candidatePorts);
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        for (int port : candidatePorts) {
            if (isAvailable(port)) {
                factory.setPort(port);
                return;
            }
        }

        throw new IllegalStateException(
                "No hay puertos libres para desarrollo. Probados: " + candidatePorts);
    }

    private boolean isAvailable(int port) {
        try (ServerSocket socket = new ServerSocket()) {
            // Sin SO_REUSEADDR: en Windows dos sockets con esa opción pueden
            // compartir el mismo puerto y daríamos un falso "libre".
            socket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 1);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
