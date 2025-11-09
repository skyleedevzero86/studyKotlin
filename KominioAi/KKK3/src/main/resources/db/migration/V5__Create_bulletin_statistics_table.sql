-- 게시판 통계 테이블 생성
CREATE TABLE bulletin_statistics (
    id VARCHAR(36) PRIMARY KEY,
    date DATE NOT NULL,
    category VARCHAR(20) NOT NULL,
    post_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_date_category (date, category),
    INDEX idx_date (date),
    INDEX idx_category (category)
);
