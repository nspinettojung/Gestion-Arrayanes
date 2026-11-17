package com.Gestion.Arrayanes.Config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class DevPortCustomizerTest {

    @Test
    void eligeElPrimerPuertoLibreDeLaLista() throws IOException {
        try (ServerSocket ocupado = reservarPuerto()) {
            int libre = puertoLibre();

            ConfigurableServletWebServerFactory factory = mock(ConfigurableServletWebServerFactory.class);
            new DevPortCustomizer(List.of(ocupado.getLocalPort(), libre)).customize(factory);

            verify(factory).setPort(libre);
        }
    }

    @Test
    void fallaCuandoTodosLosPuertosEstanOcupados() throws IOException {
        try (ServerSocket primero = reservarPuerto(); ServerSocket segundo = reservarPuerto()) {
            ConfigurableServletWebServerFactory factory = mock(ConfigurableServletWebServerFactory.class);

            assertThrows(IllegalStateException.class,
                    () -> new DevPortCustomizer(List.of(primero.getLocalPort(), segundo.getLocalPort()))
                            .customize(factory));
            verifyNoInteractions(factory);
        }
    }

    @Test
    void rechazaPuertosFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new DevPortCustomizer(List.of(70000)));
    }

    private ServerSocket reservarPuerto() throws IOException {
        ServerSocket socket = new ServerSocket();
        socket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        return socket;
    }

    private int puertoLibre() throws IOException {
        try (ServerSocket socket = reservarPuerto()) {
            return socket.getLocalPort();
        }
    }
}
