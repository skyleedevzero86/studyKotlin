CREATE TABLE IF NOT EXISTS surveys (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    participant_count INTEGER NOT NULL DEFAULT 0,
    target_type VARCHAR(20) NOT NULL DEFAULT 'ALL',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    duration VARCHAR(50),
    survey_type VARCHAR(20) NOT NULL DEFAULT 'SURVEY',
    participant_type VARCHAR(20) NOT NULL DEFAULT 'MEMBER'
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_surveys_title ON surveys(title);
CREATE INDEX IF NOT EXISTS idx_surveys_author ON surveys(author);
CREATE INDEX IF NOT EXISTS idx_surveys_status ON surveys(status);
CREATE INDEX IF NOT EXISTS idx_surveys_created_at ON surveys(created_at);