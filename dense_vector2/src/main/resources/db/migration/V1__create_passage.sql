-- =============================================================================
-- V1: 학습용 문단과 임베딩 벡터 저장
-- -----------------------------------------------------------------------------
-- 목적
--   - dense_vector2 데이터베이스의 passage 테이블에 원문과 임베딩을 보관한다.
--   - dense_vector 데이터베이스와 테이블을 공유하지 않는다.
--   - Elasticsearch 9.5 인덱스 dense-vector2-passages 가 kNN 검색을 담당한다.
--   - 정확한 코사인 재계산은 embedding_json을 사용한다.
-- 대상
--   - 테이블: passage
--   - 컬럼: id, title, body, dims, embedding_json, created_at
-- 주의
--   - title은 uk_passage_title로 유일하다. 같은 제목은 409로 거절한다.
--   - embedding_json은 쉼표로 구분한 float 나열이며 Locale.US 소수점을 쓴다.
--   - dims는 dense_vector.dims와 같은 값이어야 한다. 기본 학습 설정은 64다.
--   - 샘플 1,000건은 이 파일이 아니라 애플리케이션 기동 시 넣는다.
--   - DROP, TRUNCATE는 포함하지 않는다.
-- 연관
--   - 등록/수정/삭제/단건/목록: /api/passages
--   - 검색: POST /api/passages/search
-- =============================================================================

-- -----------------------------------------------------------------------------
-- passage: 문단 원문과 임베딩
--   - id: 자동 증가 기본키. Elasticsearch 문서 id와 같다.
--   - title: 화면과 검색 결과에 보이는 제목. 200자 이하, 중복 불가.
--   - body: 임베딩에 넣은 원문.
--   - dims: embedding_json의 차원 수.
--   - embedding_json: 해시 임베딩 벡터의 문자열 표현.
--   - created_at: 저장 시각. 마이크로초까지 남긴다.
-- -----------------------------------------------------------------------------
CREATE TABLE passage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    dims INT NOT NULL,
    embedding_json TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_passage_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
