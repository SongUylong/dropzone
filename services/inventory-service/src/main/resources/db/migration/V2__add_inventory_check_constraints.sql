ALTER TABLE inventories ADD CONSTRAINT chk_available_quantity_non_negative CHECK (available_quantity >= 0);
ALTER TABLE inventories ADD CONSTRAINT chk_reserved_quantity_non_negative CHECK (reserved_quantity >= 0);
