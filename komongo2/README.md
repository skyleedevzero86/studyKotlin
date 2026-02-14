# komongo2

## 1. 프로젝트 소개

**komongo2**는 항목(Item) CRUD와 항목별 이력(ItemLog) 조회를 제공하는 풀스택 웹 애플리케이션이다.

- **핵심 기능**: 항목 목록 조회(키워드 검색·페이징·정렬), 단건/등록/수정/삭제, 리스트 일괄 등록
- **이력**: 항목 생성/수정/삭제 시 ItemLog를 MySQL Primary·Secondary 두 DB에 이중 기록하여 조회·분석·장애 격리 등에 활용
- **기술 스택**
  - **Backend**: Kotlin 2.2, Spring Boot 4, Java 21 — MongoDB(항목), MySQL(이력 Primary/Secondary)
  - **Frontend**: Vue 3, Vue Router, TypeScript, Vite 7 — REST API 소비

---

## 2. 프로젝트 아키텍처

### 2.1 전체 구성

```
[Browser] ←→ [Vue 3 SPA (Vite dev :5173)] ←proxy /api→ [Spring Boot :8085]
                                                              │
                                    ┌─────────────────────────┼─────────────────────────┐
                                    ▼                         ▼                         ▼
                             [MongoDB :27017]          [MySQL Primary]           [MySQL Secondary]
                              collection: items        DB: soso_primary          DB: soso_secondary
                                                         table: item_log           table: item_log
```

- 프론트는 `/api`를 백엔드 8085로 프록시하여 REST 호출.
- 백엔드는 항목 저장소로 MongoDB, 이력 저장소로 MySQL Primary/Secondary를 사용하며, 항목 변경 시 두 MySQL에 동시 기록(appendBoth).

### 2.2 백엔드 레이어

| 레이어         | 패키지/역할                                                                          | 비고                                                                         |
| -------------- | ------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------- |
| API            | `item.ui.ItemController`, `itemlog.ui.ItemLogController`                             | REST, CORS, Page 직렬화(VIA_DTO)                                             |
| Application    | `item.application.ItemService`, `itemlog.application.ItemLogService`                 | 트랜잭션 경계, 이중 기록 오케스트레이션                                      |
| Domain         | `item.domain.Item`(Mongo), `itemlog.domain.ItemLog`(JPA)                             | 엔티티/도메인 모델                                                           |
| Infrastructure | `item.domain.ItemRepository`(Mongo), `itemlog.primary.*`, `itemlog.secondary.*`(JPA) | 저장소 구현                                                                  |
| Config         | `global.config`                                                                      | MongoClient(URI·@Primary), DataSource Primary/Secondary, WebSupport(VIA_DTO) |

- Item: MongoDB 단일 DB. 등록/수정/삭제 시 `ItemLogService.appendBoth(itemId, action)` 호출로 Primary·Secondary에 각각 INSERT.
- ItemLog: 동일 스키마의 `item_log` 테이블을 Primary/Secondary에 나누어 두고, 트랜잭션 매니저를 구분하여 사용.

### 2.3 프론트엔드 구조

- **경로**: `src/views`(페이지), `src/components`(공통·모달·페이징), `src/composables`(목록·폼·삭제·일괄), `src/application`(UseCase), `src/infrastructure/api`(Repository·client), `src/domain`(타입·Item).
- **데이터 흐름**: View → Composable(useItemList 등) → UseCase → Repository → REST → Backend. Page 응답은 PagedModel(content + metadata) 또는 기존 flat Page 형식 모두 `toPage()`로 정규화.

---

## 3. 테이블 명세서

### 3.1 MongoDB (database: `soso`)

#### collection: `items`

| 필드명        | 타입                | 필수 | 설명                      |
| ------------- | ------------------- | ---- | ------------------------- |
| `_id`         | ObjectId (String)   | O    | 문서 ID (Spring Data @Id) |
| `name`        | String              | O    | 항목 이름                 |
| `description` | String              | -    | 설명 (기본 "")            |
| `createdAt`   | Instant (BSON Date) | O    | 생성 시각, 인덱스 있음    |
| `updatedAt`   | Instant (BSON Date) | O    | 수정 시각                 |

- 인덱스: `createdAt` (정렬·페이징용). 키워드 검색은 name/description regex.

---

### 3.2 MySQL Primary (database: `soso_primary`)

#### table: `item_log`

| 컬럼명       | 타입         | NULL | 키                 | 설명                                   |
| ------------ | ------------ | ---- | ------------------ | -------------------------------------- |
| `id`         | BIGINT       | N    | PK, AUTO_INCREMENT | 로그 일련번호                          |
| `item_id`    | VARCHAR(255) | N    | -                  | 항목 ID (MongoDB \_id)                 |
| `action`     | VARCHAR(255) | N    | -                  | 동작 구분 (예: CREATE, UPDATE, DELETE) |
| `created_at` | TIMESTAMP 등 | N    | -                  | 기록 시각                              |

- JPA 엔티티: `ItemLog` (entityManagerFactory/transactionManager: primary). Hibernate `ddl-auto: update`로 스키마 반영.

---

### 3.3 MySQL Secondary (database: `soso_secondary`)

#### table: `item_log`

| 컬럼명       | 타입         | NULL | 키                 | 설명                               |
| ------------ | ------------ | ---- | ------------------ | ---------------------------------- |
| `id`         | BIGINT       | N    | PK, AUTO_INCREMENT | 로그 일련번호                      |
| `item_id`    | VARCHAR(255) | N    | -                  | 항목 ID (MongoDB \_id)             |
| `action`     | VARCHAR(255) | N    | -                  | 동작 구분 (CREATE, UPDATE, DELETE) |
| `created_at` | TIMESTAMP 등 | N    | -                  | 기록 시각                          |

- Primary와 동일 스키마. JPA: `ItemLog` (entityManagerFactory/transactionManager: secondary). 항목 변경 시 Primary와 동시 INSERT(appendBoth).

---

### 3.4 API 엔드포인트 요약

| Method | Path                                 | 설명                                        |
| ------ | ------------------------------------ | ------------------------------------------- |
| GET    | `/api/items`                         | 목록 (keyword, page, size, sortBy, sortDir) |
| GET    | `/api/items/{id}`                    | 단건 조회                                   |
| POST   | `/api/items`                         | 등록 (body: name, description)              |
| POST   | `/api/items/bulk`                    | 일괄 등록 (body: [{ name, description }])   |
| PUT    | `/api/items/{id}`                    | 수정 (body: name, description)              |
| DELETE | `/api/items/{id}`                    | 삭제                                        |
| GET    | `/api/items/{itemId}/logs/primary`   | 해당 항목 Primary 이력 (page, size)         |
| GET    | `/api/items/{itemId}/logs/secondary` | 해당 항목 Secondary 이력 (page, size)       |
