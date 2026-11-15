-- Código de transacción persistido y bloqueo optimista.
-- Se usa IF NOT EXISTS para tolerar bases que ya recibieron estas columnas
-- durante la etapa en la que el esquema se actualizaba con ddl-auto=update.

ALTER TABLE booking ADD COLUMN IF NOT EXISTS transaction_code VARCHAR(32);
ALTER TABLE booking ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_booking_transaction_code ON booking (transaction_code);
