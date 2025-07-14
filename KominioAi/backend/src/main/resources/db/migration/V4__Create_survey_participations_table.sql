CREATE TABLE survey_participations (
    id VARCHAR(50) PRIMARY KEY,
    survey_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50),
    participant_name VARCHAR(100),
    participant_phone VARCHAR(20),
    authenticated BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    participated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_participations_survey FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE,
    CONSTRAINT uk_participations_survey_user UNIQUE (survey_id, user_id)
);

CREATE INDEX idx_participations_survey_id ON survey_participations(survey_id);
CREATE INDEX idx_participations_user_id ON survey_participations(user_id);
CREATE INDEX idx_participations_participated_at ON survey_participations(participated_at);
CREATE INDEX idx_participations_status ON survey_participations(status);