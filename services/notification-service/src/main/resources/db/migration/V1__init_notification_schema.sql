CREATE TABLE notification_records (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    event_type VARCHAR(100),
    order_number VARCHAR(100),
    user_id VARCHAR(255),
    message_text TEXT,
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE ticket_records (
    id BIGSERIAL PRIMARY KEY,
    ticket_id VARCHAR(100) NOT NULL UNIQUE,
    order_number VARCHAR(100) NOT NULL,
    user_id VARCHAR(255),
    event_name VARCHAR(255),
    category_name VARCHAR(100),
    seat_number VARCHAR(50),
    event_date VARCHAR(100),
    qr_code_url VARCHAR(512),
    pdf_url VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE job_records (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(100) NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    target_queue VARCHAR(100),
    order_number VARCHAR(100),
    user_id VARCHAR(255),
    details TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_notification_records_order ON notification_records(order_number);
CREATE INDEX idx_notification_records_user ON notification_records(user_id);
CREATE INDEX idx_ticket_records_order ON ticket_records(order_number);
CREATE INDEX idx_job_records_order ON job_records(order_number);
CREATE INDEX idx_job_records_queue ON job_records(target_queue);
