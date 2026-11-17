-- Borrado logico y archivado de reservas.
ALTER TABLE booking ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL;

-- Las reservas ya finalizadas quedan como historial consultable.
UPDATE booking SET status = 'ARCHIVED'
WHERE status = 'ACTIVE' AND departure_date < CURRENT_DATE;

CREATE INDEX IF NOT EXISTS idx_booking_status ON booking (status);
CREATE INDEX IF NOT EXISTS idx_booking_status_departure ON booking (status, departure_date);
