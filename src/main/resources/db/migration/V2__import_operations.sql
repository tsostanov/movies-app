CREATE TABLE import_operations (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(255) NOT NULL,
    status        VARCHAR(32)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at  TIMESTAMPTZ,
    added_count   INTEGER,
    error_message VARCHAR(2000)
);

CREATE INDEX idx_import_operations_created_at
    ON import_operations (created_at DESC);
