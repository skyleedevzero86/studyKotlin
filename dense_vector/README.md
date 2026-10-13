# dense_vector

MySQL에 문단 원문과 임베딩을 두고, Elasticsearch `dense_vector`로 코사인 유사도 kNN 검색을 해 보는 Kotlin 학습 프로젝트다. <br/>
임베딩은 언어 모델이 아니라 문자 2-gram 해시다. <br/>
같은 문장은 항상 같은 벡터가 되고, 비슷한 문장은 방향이 가깝게 나오도록 맞춰 둔 학습용 벡터다.
<br/>

## 실행

백엔드는 MySQL 8과 Elasticsearch 8.15를 쓴다. <br/>

- MySQL: `jdbc:mysql://127.0.0.1:3306/dense_vector`
- Elasticsearch: `http://127.0.0.1:9200`
- 인덱스: `dense-vector-passages`
- 매핑: dims 64, similarity `cosine`, index type `hnsw`, m 16, ef_construction 100

```text
cd dense_vector
.\gradlew.bat bootRun
```

기동하면 인덱스를 확인하고, 비어 있는 제목만 샘플 6건을 넣은 뒤 문단이 1,000건이 될 때까지 가상 문단을 채운다. <br/>
이미 1,000건 이상이면 추가로 넣지 않는다. <br/>
`study.vector.startup-seed` 기본값은 `true`다. <Br/>
`false`로 두면 인덱스 준비만 하고 적재는 건너뛴다.<br/>

프론트는 Vite + React 19 + React Router다.

```text
cd dense_vector/frontend
pnpm install
pnpm dev
```

화면은 http://127.0.0.1:5173 이다.
`/api`는 8088로 프록시된다. 목록, 등록, 상세, 수정, 삭제, 유사도 검색을 할 수 있다.<br/>

## 패키지

`com.sleekydz86.densevector` 아래다. 등록·수정·삭제·단건·목록은 유스케이스마다 요청, 서비스, 컨트롤러를 나눈다. <br/>

나머지 공통 역할은 `global`이다.
<br/>

| 패키지                 | 역할                                                           |
| ---------------------- | -------------------------------------------------------------- |
| `passage.create`       | `POST /api/passages`                                           |
| `passage.update`       | `PUT /api/passages/{id}`                                       |
| `passage.delete`       | `DELETE /api/passages/{id}`                                    |
| `passage.get`          | `GET /api/passages/{id}`                                       |
| `passage.list`         | `GET /api/passages?page&size`                                  |
| `global.search`        | `POST /api/passages/search`                                    |
| `global.seed`          | 샘플 6건, 1,000건까지 채우기, `POST /api/passages/samples`     |
| `global.elasticsearch` | 인덱스 준비, 문서 색인/삭제, kNN 조회, `GET /api/vector-index` |
| `global.embedding`     | 해시 임베딩, 코사인, dims·HNSW 규칙, 벡터 문자열               |
| `global.persistence`   | JPA 엔티티, 조회, dims 검사                                    |
| `global.passage`       | 목록/상세 응답으로 바꾸는 변환                                 |
| `global.config`        | DB·ES 클라이언트, 기동 시 적재                                 |
| `global.error`         | 한국어 오류 응답                                               |

## 데이터

스키마는 Flyway `V1__create_passage.sql` 한 파일이다. 테이블 `passage` 컬럼은 `id`, `title`(unique), `body`, `dims`, `embedding_json`, `created_at`이다. <br/>
이 마이그레이션과 `flyway_schema_history`는 애플리케이션이 임의로 고치지 않는다. <br/>
샘플과 1,000건 채우기는 Flyway INSERT가 아니라 기동 시 서비스가 한다. <br/>
Elasticsearch와 같이 맞춰야 해서다. <Br/>
제목은 유니크다. 채우는 문단 제목은 `가상 문단 0001` 형식이고 본문은 개발용 가상 문장이다.<br/>

dims나 인덱스 타입을 바꾸면 기존 Elasticsearch 인덱스, 그리고 이미 저장된 `passage.dims`와 맞지 않아 기동이 실패한다. <br/>
그 경우 인덱스를 지우고 테이블을 비운 뒤 다시 실행해야 한다. 앱은 DROP/TRUNCATE를 실행하지 않는다.<br/>

## 테스트

한 번에 실행한다. 통합 테스트는 Docker가 필요하다.

```text
cd dense_vector
.\gradlew.bat test
```

| 종류 | 위치                                                                   | 무엇을 보나                                                                                                                                                                                                                  |
| ---- | ---------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 단위 | `src/test/.../unit`, `global/embedding`, `passage/list`, `global/seed` | 서비스, 페이지 범위, 임베딩, 가상 문단, 색인 실패 시 행 삭제. Spring과 DB 없이 메모리 저장소와 기록용 인덱스 대역을 쓴다.                                                                                                    |
| 기능 | `src/test/.../feature`                                                 | MockMvc로 상태 코드, 경로, 검증 메시지. 서비스는 대역이다.                                                                                                                                                                   |
| 통합 | `src/test/.../integration/PassageApiIntegrationTest.kt`                | Testcontainers MySQL 8.4와 Elasticsearch 8.15.3에 등록, 단건, 검색, 수정, 삭제, 중복 제목 409을 태운다. `startup-seed=false`라 1,000건을 넣지 않는다. 로컬 `dense_vector` DB와 `dense-vector-passages` 인덱스는 쓰지 않는다. |

## 기술

- Kotlin 2.4, Spring Boot 4.1, Java 25
- Spring Data JPA, Flyway, MySQL
- Elasticsearch Java 클라이언트 8.15.3
- springdoc OpenAPI
- 프론트: pnpm, Vite, React 19, React Router
