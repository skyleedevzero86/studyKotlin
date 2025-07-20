CREATE TABLE user_two_factor_auth (
                                      id VARCHAR(36) PRIMARY KEY,
                                      user_id VARCHAR(36) NOT NULL,
                                      secret_key VARCHAR(255) NOT NULL,
                                      backup_codes JSON,
                                      backup_codes_used JSON,
                                      sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                      email_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                      app_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                                      last_used_at TIMESTAMP,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                      INDEX idx_user_two_factor_auth_user_id (user_id),

                                      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);