CREATE TABLE IF NOT EXISTS post
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    FULLTEXT INDEX idx_ft_title (title) COMMENT 'parser "TokenBigram"',
    FULLTEXT INDEX idx_ft_content (content) COMMENT 'parser "TokenBigram"'
    ) ENGINE=Mroonga DEFAULT CHARSET=utf8mb4 COMMENT='engine "InnoDB"';