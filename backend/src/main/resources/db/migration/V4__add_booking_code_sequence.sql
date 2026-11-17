-- Secuencia de códigos de transacción protegida con bloqueo pesimista.
-- Fila única: al cambiar de año se reinicia desde los códigos ya persistidos.
CREATE TABLE IF NOT EXISTS booking_code_sequence (
    id INT NOT NULL PRIMARY KEY,
    sequence_year INT NOT NULL,
    next_value BIGINT NOT NULL
);

-- year=0 fuerza la inicialización desde booking la primera vez que se pida un código.
MERGE INTO booking_code_sequence (id, sequence_year, next_value) KEY (id) VALUES (1, 0, 1);
