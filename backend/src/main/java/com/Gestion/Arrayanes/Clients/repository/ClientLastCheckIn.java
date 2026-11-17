package com.Gestion.Arrayanes.Clients.repository;

import java.time.LocalDate;
import java.util.UUID;

/** Último check-in por cliente, calculado en una sola consulta agrupada. */
public interface ClientLastCheckIn {

    UUID getClientId();

    LocalDate getLastCheckIn();
}
