CREATE TABLE refresh_token
(
    id         UUID                        NOT NULL,
    username   VARCHAR(255)                NOT NULL,
    issued_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    revoked    BOOLEAN                     NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id)
);

CREATE INDEX IDX_REFRESH_TOKEN_USERNAME ON refresh_token (username);

CREATE INDEX IDX_REFRESH_TOKEN_EXPIRES_AT ON refresh_token (expires_at);
