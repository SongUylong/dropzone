CREATE TABLE audit_records (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    event_type VARCHAR(100),
    order_number VARCHAR(100),
    user_id VARCHAR(255),
    payload TEXT,
    formatted_message TEXT,
    received_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_records_topic ON audit_records(topic);
CREATE INDEX idx_audit_records_order_number ON audit_records(order_number);
CREATE INDEX idx_audit_records_event_type ON audit_records(event_type);
