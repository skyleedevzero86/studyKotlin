CREATE TABLE auth_tokens (
                             id VARCHAR(36) PRIMARY KEY,
                             user_id VARCHAR(36) NOT NULL,
                             token_type VARCHAR(20) NOT NULL,
                             access_token TEXT NOT NULL,
                             refresh_token VARCHAR(255),
                             expires_at TIMESTAMP NOT NULL,
                             issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             device_info TEXT,
                             ip_address VARCHAR(45),
                             user_agent TEXT,
                             revoked BOOLEAN NOT NULL DEFAULT FALSE,
                             revoked_at TIMESTAMP,
                             revoked_reason VARCHAR(100),

                             INDEX idx_auth_tokens_user_id (user_id),
                             INDEX idx_auth_tokens_token_type (token_type),
                             INDEX idx_auth_tokens_expires_at (expires_at),
                             INDEX idx_auth_tokens_revoked (revoked),
                             INDEX idx_auth_tokens_refresh_token (refresh_token),

                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);