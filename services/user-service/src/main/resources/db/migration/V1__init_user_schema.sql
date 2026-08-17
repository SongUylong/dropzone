CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    keycloak_user_id VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    preferences TEXT,
    purchase_history_ref VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (name, email, status, preferences, purchase_history_ref)
VALUES ('John Smith', 'john@example.com', 'ACTIVE', '{"theme":"dark","notifications":true}', 'purchase-history-ref-1001');
