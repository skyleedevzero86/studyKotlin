CREATE TABLE user_roles (
                            id VARCHAR(36) PRIMARY KEY,
                            user_id VARCHAR(36) NOT NULL,
                            role_name VARCHAR(50) NOT NULL,
                            granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            granted_by VARCHAR(36),
                            active BOOLEAN NOT NULL DEFAULT TRUE,

                            INDEX idx_user_roles_user_id (user_id),
                            INDEX idx_user_roles_role_name (role_name),
                            INDEX idx_user_roles_active (active),

                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            UNIQUE KEY uk_user_role (user_id, role_name)
);