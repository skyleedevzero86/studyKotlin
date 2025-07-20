CREATE TABLE rate_limiting (
                               id VARCHAR(36) PRIMARY KEY,
                               key_name VARCHAR(255) NOT NULL,
                               key_type VARCHAR(50) NOT NULL,
                               count INT NOT NULL DEFAULT 1,
                               window_start TIMESTAMP NOT NULL,
                               window_end TIMESTAMP NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                               INDEX idx_rate_limiting_key_name (key_name),
                               INDEX idx_rate_limiting_key_type (key_type),
                               INDEX idx_rate_limiting_window_end (window_end),
                               UNIQUE KEY uk_rate_limiting_key_window (key_name, window_start)
);