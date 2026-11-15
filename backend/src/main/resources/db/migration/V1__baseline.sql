-- Esquema inicial previo a la incorporación de Flyway.
-- Las bases existentes se marcan como baseline en esta versión;
-- las bases nuevas se crean ejecutando este script.

CREATE TABLE clients (
    id UUID NOT NULL,
    client_name VARCHAR(255),
    client_last_name VARCHAR(255),
    client_dni VARCHAR(255),
    client_phone VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE apartment (
    id UUID NOT NULL,
    apartment_name VARCHAR(255),
    apartment_max_number BIGINT,
    price NUMERIC(38, 2),
    PRIMARY KEY (id),
    CONSTRAINT uk_apartment_name_max_number UNIQUE (apartment_name, apartment_max_number)
);

CREATE TABLE booking (
    id UUID NOT NULL,
    arrival_date DATE,
    nights BIGINT,
    departure_date DATE,
    amount_people BIGINT,
    total_price NUMERIC(38, 2),
    client_id UUID NOT NULL,
    apartment_id UUID NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_booking_client FOREIGN KEY (client_id) REFERENCES clients (id),
    CONSTRAINT fk_booking_apartment FOREIGN KEY (apartment_id) REFERENCES apartment (id)
);

CREATE TABLE price (
    id UUID NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    apartment_id UUID NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_price_apartment FOREIGN KEY (apartment_id) REFERENCES apartment (id)
);
