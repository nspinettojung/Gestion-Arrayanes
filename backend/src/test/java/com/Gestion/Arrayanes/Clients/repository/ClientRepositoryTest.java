package com.Gestion.Arrayanes.Clients.repository;

import com.Gestion.Arrayanes.Clients.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        clientRepository.save(new Client(null, "Ana", "Perez", "12345678", "555-1234"));
    }

    @Test
    void shouldDetectExistingDni() {
        assertTrue(clientRepository.existsByClientDni("12345678"));
        assertFalse(clientRepository.existsByClientDni("00000000"));
    }

    @Test
    void shouldFindClientByDni() {
        Optional<Client> found = clientRepository.findByClientDni("12345678");

        assertTrue(found.isPresent());
        assertEquals("Ana", found.get().getClientName());
    }

    @Test
    void shouldReturnEmptyWhenDniDoesNotExist() {
        assertTrue(clientRepository.findByClientDni("00000000").isEmpty());
    }
}
