CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(100) NOT NULL UNIQUE,
    order_number VARCHAR(50) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD' NOT NULL,
    status VARCHAR(50) NOT NULL,
    mode VARCHAR(50) NOT NULL,
    failure_reason VARCHAR(255),
    transaction_id VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_payments_order_number ON payments(order_number);
CREATE INDEX idx_payments_status ON payments(status);
