CREATE TABLE IF NOT EXISTS public.users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(512),
    email_hash VARCHAR(64),
    roles VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS public.password_change_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES public.users(id),
    changed_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS public.password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_value VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES public.users(id),
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS public.one_time_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_value VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP NOT NULL
);
