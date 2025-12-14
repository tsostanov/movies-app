ALTER TABLE import_operations
    ADD COLUMN IF NOT EXISTS tx_id varchar(128),
    ADD COLUMN IF NOT EXISTS original_filename varchar(255),
    ADD COLUMN IF NOT EXISTS file_content_type varchar(255),
    ADD COLUMN IF NOT EXISTS staging_key varchar(512),
    ADD COLUMN IF NOT EXISTS file_key varchar(512);

UPDATE import_operations
SET tx_id = COALESCE(tx_id, 'legacy-' || id::text)
WHERE tx_id IS NULL;

ALTER TABLE import_operations
    ALTER COLUMN tx_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_import_operations_tx_id ON import_operations (tx_id);
