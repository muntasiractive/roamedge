-- Initial Schema for Roam Application
-- H2 Database DDL

-- Regions table
CREATE TABLE regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(7) NOT NULL,
    is_default BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Operations table
CREATE TABLE operations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    purpose TEXT,
    due_date DATE,
    status VARCHAR(20),
    outcome TEXT,
    priority VARCHAR(10),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    region VARCHAR(50)
);

-- Calendar Sources table
CREATE TABLE calendar_sources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_visible BOOLEAN NOT NULL,
    is_default BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Calendar Events table
CREATE TABLE calendar_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    calendar_source_id BIGINT NOT NULL,
    operation_id BIGINT,
    task_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    start_date_time TIMESTAMP NOT NULL,
    end_date_time TIMESTAMP NOT NULL,
    is_all_day BOOLEAN NOT NULL,
    color VARCHAR(7),
    recurrence_rule TEXT,
    recurrence_end_date TIMESTAMP,
    parent_event_id BIGINT,
    is_recurring_instance BOOLEAN NOT NULL,
    original_start_date_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    region VARCHAR(50),
    wiki_id BIGINT,
    CONSTRAINT fk_calendar_event_source FOREIGN KEY (calendar_source_id) REFERENCES calendar_sources(id) ON DELETE CASCADE,
    CONSTRAINT fk_calendar_event_operation FOREIGN KEY (operation_id) REFERENCES operations(id) ON DELETE SET NULL,
    CONSTRAINT fk_calendar_event_parent FOREIGN KEY (parent_event_id) REFERENCES calendar_events(id) ON DELETE CASCADE
);

-- Wiki Templates table
CREATE TABLE wiki_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    content TEXT NOT NULL,
    icon VARCHAR(10),
    is_default BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Wikis table
CREATE TABLE wikis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_id BIGINT,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    is_favorite BOOLEAN DEFAULT FALSE,
    template_id BIGINT,
    word_count INTEGER DEFAULT 0,
    linked_wiki_ids TEXT,
    region VARCHAR(50),
    task_id BIGINT,
    calendar_event_id BIGINT,
    banner_url VARCHAR(512),
    CONSTRAINT fk_wiki_operation FOREIGN KEY (operation_id) REFERENCES operations(id) ON DELETE SET NULL,
    CONSTRAINT fk_wiki_template FOREIGN KEY (template_id) REFERENCES wiki_templates(id) ON DELETE SET NULL
);

-- Tasks table
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    due_date TIMESTAMP,
    assignee VARCHAR(100),
    priority VARCHAR(10) NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    region VARCHAR(50),
    calendar_event_id BIGINT,
    wiki_id BIGINT,
    CONSTRAINT fk_task_operation FOREIGN KEY (operation_id) REFERENCES operations(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_calendar_event FOREIGN KEY (calendar_event_id) REFERENCES calendar_events(id) ON DELETE SET NULL,
    CONSTRAINT fk_task_wiki FOREIGN KEY (wiki_id) REFERENCES wikis(id) ON DELETE SET NULL
);

-- Journal Templates table
CREATE TABLE journal_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Journal Entries table
CREATE TABLE journal_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Add foreign key constraints for cross-references
ALTER TABLE calendar_events ADD CONSTRAINT fk_calendar_event_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL;
ALTER TABLE calendar_events ADD CONSTRAINT fk_calendar_event_wiki FOREIGN KEY (wiki_id) REFERENCES wikis(id) ON DELETE SET NULL;

-- Create indexes for better query performance
CREATE INDEX idx_operations_status ON operations(status);
CREATE INDEX idx_operations_region ON operations(region);
CREATE INDEX idx_operations_due_date ON operations(due_date);
CREATE INDEX idx_operations_priority ON operations(priority);

CREATE INDEX idx_tasks_operation_id ON tasks(operation_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_region ON tasks(region);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_calendar_event_id ON tasks(calendar_event_id);
CREATE INDEX idx_tasks_wiki_id ON tasks(wiki_id);

CREATE INDEX idx_wikis_operation_id ON wikis(operation_id);
CREATE INDEX idx_wikis_region ON wikis(region);
CREATE INDEX idx_wikis_is_favorite ON wikis(is_favorite);
CREATE INDEX idx_wikis_template_id ON wikis(template_id);
CREATE INDEX idx_wikis_task_id ON wikis(task_id);
CREATE INDEX idx_wikis_calendar_event_id ON wikis(calendar_event_id);

CREATE INDEX idx_calendar_events_source_id ON calendar_events(calendar_source_id);
CREATE INDEX idx_calendar_events_operation_id ON calendar_events(operation_id);
CREATE INDEX idx_calendar_events_task_id ON calendar_events(task_id);
CREATE INDEX idx_calendar_events_wiki_id ON calendar_events(wiki_id);
CREATE INDEX idx_calendar_events_start_date_time ON calendar_events(start_date_time);
CREATE INDEX idx_calendar_events_end_date_time ON calendar_events(end_date_time);
CREATE INDEX idx_calendar_events_region ON calendar_events(region);
CREATE INDEX idx_calendar_events_parent_event_id ON calendar_events(parent_event_id);

CREATE INDEX idx_calendar_sources_type ON calendar_sources(type);
CREATE INDEX idx_calendar_sources_is_visible ON calendar_sources(is_visible);

CREATE INDEX idx_journal_entries_date ON journal_entries(date);

CREATE INDEX idx_regions_name ON regions(name);
CREATE INDEX idx_regions_is_default ON regions(is_default);