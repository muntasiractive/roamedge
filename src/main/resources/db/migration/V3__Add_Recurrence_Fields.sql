-- Add recurrence fields to tasks table
ALTER TABLE tasks ADD COLUMN recurrence_rule TEXT;
ALTER TABLE tasks ADD COLUMN recurrence_end_date TIMESTAMP;
ALTER TABLE tasks ADD COLUMN parent_task_id BIGINT;
ALTER TABLE tasks ADD COLUMN is_recurring_instance BOOLEAN DEFAULT FALSE NOT NULL;

