# dense_vector2

`dense_vector`를 복사한 뒤 버전 프로젝트다. <br/>
화면과 저장 구조는 같고, 인덱스만 Elasticsearch 8.15보다 뒤인 기능을 쓴다.<br/>
임베딩은 언어 모델이 아니라 문자 2-gram 해시다.<br/>

- `bbq_hnsw`: Elasticsearch 9.1부터. 기본 `index-type`이다.<br/>
- `m`, `ef_construction`을 쓴다.<br/>
- `bbq_disk`: Elasticsearch 9.4부터. Enterprise 라이선스가 필요하다.
- `study.vector.index-type: bbq_disk`로 바꾼다.<br/>
- HNSW의 `m`, `ef_construction`은 쓰지 않는다.<br/>
- `vectordb_document`: Elasticsearch 9.5부터. 인덱스 생성 시 `index.mode`로 넣는다.<br/>
- 벡터는 `_source`에 남기지 않고, 코사인 재계산은 MySQL `embedding_json`으로 한다.

  8.15.3에 붙이면 기동이 거절된다. <br/>
  그 클러스터는 `dense_vector`만 쓴다.<br/>

## 실행

백엔드는 MySQL 8과, 이 디렉터리의 Elasticsearch 9.5.5가 필요하다. <br/>
9200의 8.15 클러스터와 데이터 볼륨을 공유하지 않는다.<br/>

```text
cd dense_vector2
docker compose up -d
```

- 서버: http://127.0.0.1:8089
- MySQL: `jdbc:mysql://127.0.0.1:3306/dense_vector2`
- Elasticsearch: http://127.0.0.1:9201 (클러스터 이름 `dense-vector2`)
- 로컬 라이선스: trial. `bbq_disk`는 Enterprise 기능이라 이 학습 클러스터에서만 trial로 연다.
- 인덱스: `dense-vector2-passages`
- 매핑: `vectordb_document`, dims 64, similarity `cosine`, index type `bbq_hnsw`, m 16, ef_construction 100

데이터베이스 `dense_vector2`는 비어 있는 개발 DB로 직접 만든 뒤 실행한다. <br/>
이 프로젝트는 DROP/TRUNCATE를 하지 않는다.<br/>

```text
cd dense_vector2
.\gradlew.bat bootRun
```

기동하면 인덱스를 확인하고, 비어 있는 제목만 샘플 6건을 넣은 뒤 문단이 1,000건이 될 때까지 가상 문단을 채운다. <br/>
이미 1,000건 이상이면 추가로 넣지 않는다. <br/>
`study.vector.startup-seed` 기본값은 `true`다. <Br/>
`false`로 두면 인덱스 준비만 하고 적재는 건너뛴다.<br/>

프론트는 Vite + React 19 + React Router다.<br/>

```text
cd dense_vector2/frontend
pnpm install
pnpm dev
```

화면은 http://127.0.0.1:5174 이다.
`/api`는 8089로 프록시된다. 목록, 등록, 상세, 수정, 삭제, 유사도 검색을 할 수 있다.<br/>

## 패키지

`com.sleekydz86.densevector2` 아래다. 등록·수정·삭제·단건·목록은 유스케이스마다 요청, 서비스, 컨트롤러를 나눈다. <br/>

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
cd dense_vector2
.\gradlew.bat test
```

| 종류 | 위치                                                                   | 무엇을 보나                                                                                                                                                                                                                   |
| ---- | ---------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 단위 | `src/test/.../unit`, `global/embedding`, `passage/list`, `global/seed` | 서비스, 페이지 범위, 임베딩, 가상 문단, 색인 실패 시 행 삭제. Spring과 DB 없이 메모리 저장소와 기록용 인덱스 대역을 쓴다.                                                                                                     |
| 기능 | `src/test/.../feature`                                                 | MockMvc로 상태 코드, 경로, 검증 메시지. 서비스는 대역이다.                                                                                                                                                                    |
| 통합 | `src/test/.../integration/PassageApiIntegrationTest.kt`                | Testcontainers MySQL 8.4와 Elasticsearch 9.5.0에 등록, 단건, 검색, 수정, 삭제, 중복 제목 409을 태운다. `startup-seed=false`라 1,000건을 넣지 않는다. 로컬 `dense_vector2` DB와 `dense-vector2-passages` 인덱스는 쓰지 않는다. |

## 기술

- Kotlin 2.4, Spring Boot 4.1, Java 25
- Spring Data JPA, Flyway, MySQL
- Elasticsearch Java 클라이언트 8.15.3. 인덱스를 만드는 대상 클러스터는 9.5 이상이다.
- springdoc OpenAPI
- 프론트: pnpm, Vite, React 19, React Router
