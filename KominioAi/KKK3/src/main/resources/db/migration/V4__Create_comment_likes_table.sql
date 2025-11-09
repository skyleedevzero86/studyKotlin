-- 댓글 좋아요 테이블 생성
CREATE TABLE comment_likes (
    id VARCHAR(36) PRIMARY KEY,
    comment_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    UNIQUE KEY uk_comment_user (comment_id, user_id),
    
    INDEX idx_comment_id (comment_id),
    INDEX idx_user_id (user_id)
);
