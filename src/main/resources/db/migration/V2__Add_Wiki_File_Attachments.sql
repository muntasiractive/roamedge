-- Add wiki_file_attachments table
CREATE TABLE wiki_file_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wiki_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(100),
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_wiki_file_attachments_wiki FOREIGN KEY (wiki_id) REFERENCES wikis(id) ON DELETE CASCADE
);

-- Create index on wiki_id for faster lookup
CREATE INDEX idx_wiki_file_attachments_wiki_id ON wiki_file_attachments(wiki_id);

-- Create index on created_at for sorting
CREATE INDEX idx_wiki_file_attachments_created_at ON wiki_file_attachments(created_at DESC);
