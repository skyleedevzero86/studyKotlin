CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       profile VARCHAR(255),
                       status VARCHAR(20) NOT NULL,
                       two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL,
                       last_login_at TIMESTAMP
);

CREATE TABLE user_roles (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(50) NOT NULL UNIQUE,
                            permissions TEXT NOT NULL,
                            description VARCHAR(255),
                            enabled BOOLEAN NOT NULL
);

CREATE TABLE auth_tokens (
                             id UUID PRIMARY KEY,
                             user_id UUID NOT NULL,
                             access_token TEXT NOT NULL,
                             refresh_token TEXT NOT NULL,
                             type VARCHAR(20) NOT NULL,
                             expires_at TIMESTAMP NOT NULL,
                             issued_at TIMESTAMP NOT NULL,
                             device_info VARCHAR(255),
                             FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_social_accounts (
                                      id BIGSERIAL PRIMARY KEY,
                                      user_id UUID NOT NULL,
                                      provider VARCHAR(20) NOT NULL,
                                      provider_user_id VARCHAR(255) NOT NULL,
                                      linked_at TIMESTAMP NOT NULL,
                                      FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_security_logs (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id UUID NOT NULL,
                                    event VARCHAR(255) NOT NULL,
                                    ip VARCHAR(50),
                                    created_at TIMESTAMP NOT NULL,
                                    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_two_factor_auth (
                                      id BIGSERIAL PRIMARY KEY,
                                      user_id UUID NOT NULL,
                                      secret VARCHAR(255) NOT NULL,
                                      backup_codes TEXT,
                                      enabled BOOLEAN NOT NULL,
                                      created_at TIMESTAMP NOT NULL,
                                      FOREIGN KEY (user_id) REFERENCES users(id)
);