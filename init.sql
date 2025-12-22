-- init.sql - Database initialization
CREATE TABLE jobs (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    payload TEXT,
    progress INTEGER DEFAULT 0,
    result TEXT,
    error TEXT,
    retry_count INTEGER DEFAULT 0,
    submitted_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    metadata JSONB
);

CREATE INDEX idx_idempotency ON jobs(idempotency_key);
CREATE INDEX idx_status ON jobs(status);
CREATE INDEX idx_submitted ON jobs(submitted_at);
