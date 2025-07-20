CREATE TABLE user_social_accounts (
                                      id VARCHAR(36) PRIMARY KEY,
                                      user_id VARCHAR(36) NOT NULL,
                                      provider VARCHAR(20) NOT NULL,
                                      provider_user_id VARCHAR(255) NOT NULL,
                                      email VARCHAR(255),
                                      display_name VARCHAR(255),
                                      profile_image_url VARCHAR(500),
                                      access_token TEXT,
                                      refresh_token TEXT,
                                      token_expires_at TIMESTAMP,
                                      connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      last_synced_at TIMESTAMP,
                                      active BOOLEAN NOT NULL DEFAULT TRUE,

                                      INDEX idx_user_social_accounts_user_id (user_id),
                                      INDEX idx_user_social_accounts_provider (provider),
                                      INDEX idx_user_social_accounts_provider_user_id (provider_user_id),
                                      INDEX idx_user_social_accounts_active (active),

                                      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                      UNIQUE KEY uk_provider_user_id (provider, provider_user_id)
);