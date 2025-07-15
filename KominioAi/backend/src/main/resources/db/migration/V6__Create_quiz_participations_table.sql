CREATE TABLE quiz_participations (
    id VARCHAR(36) PRIMARY KEY,
    survey_id VARCHAR(36) NOT NULL,
    participant_name VARCHAR(100) NOT NULL,
    participant_phone VARCHAR(20) NOT NULL,
    user_id VARCHAR(36),
    started_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_quiz_participations_survey_id (survey_id),
    INDEX idx_quiz_participations_participant_phone (participant_phone),
    INDEX idx_quiz_participations_status (status),
    INDEX idx_quiz_participations_started_at (started_at),
    
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE
);