CREATE TABLE quiz_answers (
    id VARCHAR(36) PRIMARY KEY,
    participation_id VARCHAR(36) NOT NULL,
    question_id VARCHAR(36) NOT NULL,
    answer_type VARCHAR(20) NOT NULL,
    answer_content TEXT NOT NULL,
    submitted_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_quiz_answers_participation_id (participation_id),
    INDEX idx_quiz_answers_question_id (question_id),
    INDEX idx_quiz_answers_submitted_at (submitted_at),
    
    FOREIGN KEY (participation_id) REFERENCES quiz_participations(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
); 