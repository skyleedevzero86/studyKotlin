-- =============================================================================
-- V1: 배역 테이블
-- -----
-- 목적
-- - 스키마 분리와 DB 분리가 같은 테이블 구조를 쓴다
-- - 테넌트 컬럼은 두지 않는다. 스키마 또는 DB 가 테넌트를 가른다
-- 주의
-- - 스키마 분리에서는 테넌트 스키마마다 이 스크립트를 실행한다
-- =============================================================================

CREATE TABLE casting (
    actor_slug VARCHAR(80) PRIMARY KEY,
    character_name VARCHAR(120) NOT NULL
);

COMMENT ON TABLE casting IS '배우 배역. 테넌트 스키마 또는 테넌트 DB 안에 둔다.';
COMMENT ON COLUMN casting.actor_slug IS '배우 식별자. 테이블 기본 키.';
COMMENT ON COLUMN casting.character_name IS '그 테넌트에서의 배역 이름.';
