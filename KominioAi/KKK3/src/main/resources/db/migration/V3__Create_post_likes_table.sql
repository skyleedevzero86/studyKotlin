-- 게시글 좋아요 테이블 생성
CREATE TABLE post_likes (
    id VARCHAR(36) PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE KEY uk_post_user (post_id, user_id),
    
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
);
