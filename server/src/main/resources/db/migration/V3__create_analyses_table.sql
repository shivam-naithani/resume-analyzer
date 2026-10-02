CREATE TABLE analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    job_description_text TEXT NOT NULL,
    match_score DOUBLE NOT NULL,
    missing_keywords JSON,
    section_scores JSON,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_analyses_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);
