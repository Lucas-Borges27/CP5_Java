-- Fix outbox_event.id column type to support binary UUID
ALTER TABLE outbox_event MODIFY COLUMN id BINARY(16) NOT NULL;