package com.Gestion.Arrayanes.Clients.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @Column(name = "client_name")
    private String clientName;
    @Column(name = "client_last_name")
    private String clientLastName;
    @Column(name = "client_dni")
    private String clientDni;
    @Column(name = "client_phone")
    private String clientPhone;

    public Client() {
    }

    public Client(UUID id, String clientName, String clientLastName, String clientDni, String clientPhone) {
        this.id = id;
        this.clientName = clientName;
        this.clientLastName = clientLastName;
        this.clientDni = clientDni;
        this.clientPhone = clientPhone;
    }

}
