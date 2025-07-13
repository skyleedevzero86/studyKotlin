CREATE TABLE IF NOT EXISTS survey_results (
    id BIGSERIAL PRIMARY KEY,
    survey_id BIGINT NOT NULL REFERENCES surveys(id),
    question_order INTEGER NOT NULL,
    question_content TEXT NOT NULL,
    answer TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_survey_results_survey_id ON survey_results(survey_id); 