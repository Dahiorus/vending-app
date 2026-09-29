ALTER TABLE refresh_token
    ADD COLUMN revoked_at  TIMESTAMP WITHOUT TIME ZONE NULL,
    ADD COLUMN replaced_by UUID NULL;

ALTER TABLE refresh_token
    ADD CONSTRAINT fk_refresh_token_replaced_by FOREIGN KEY (replaced_by) REFERENCES refresh_token (id);
