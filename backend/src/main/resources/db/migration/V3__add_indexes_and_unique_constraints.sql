-- Índices para las consultas del dashboard y la detección de solapamientos.
CREATE INDEX IF NOT EXISTS idx_booking_arrival_date ON booking (arrival_date);
CREATE INDEX IF NOT EXISTS idx_booking_departure_date ON booking (departure_date);
CREATE INDEX IF NOT EXISTS idx_booking_apartment_departure ON booking (apartment_id, departure_date);

-- Unicidad de DNI: se normaliza lo existente (sin espacios y en mayúsculas)
-- para que coincida con lo que guarda la aplicación.
UPDATE clients SET client_dni = UPPER(TRIM(client_dni)) WHERE client_dni IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_clients_client_dni ON clients (client_dni);

-- Un solo departamento por nombre, sin importar la capacidad.
UPDATE apartment SET apartment_name = TRIM(apartment_name) WHERE apartment_name IS NOT NULL;
ALTER TABLE apartment DROP CONSTRAINT IF EXISTS uk_apartment_name_max_number;
CREATE UNIQUE INDEX IF NOT EXISTS uk_apartment_name ON apartment (apartment_name);

-- Tabla huérfana del esquema inicial: no tiene entidad ni uso.
DROP TABLE IF EXISTS price;
