ALTER TABLE payments ADD CONSTRAINT uk_payments_order_number UNIQUE (order_number);
