# youtube_download

유튜브 공유 주소를 붙여 넣으면 MySQL에 요청을 남기고, 변경을 Kafka로 넘긴다. <br/>
백엔드는 그 이벤트를 받아 `yt-dlp`로 받은 파일을 MinIO에 올리고, 목록은 Elasticsearch에서 페이지로 읽는다. <br/>
제목·파일명·영상 ID는 64차원 해시 벡터로 MySQL에 남긴다. <br/>
벡터는 `_source`에 없으므로 코사인은 MySQL에서 다시 계산한다. <br/>
받을 권리가 있는 영상만 저장한다.

## 이미 떠 있는 것

| 구성                | 주소                                                 |
| ------------------- | ---------------------------------------------------- |
| MySQL 8.4           | `127.0.0.1:3306` 데이터베이스 `youtube_download`     |
| Elasticsearch 9.5.5 | `http://127.0.0.1:9201` 인덱스 `youtube-downloads`   |
| MinIO               | `http://127.0.0.1:9000` 버킷 `youtube-download`      |
| Redis 7             | `127.0.0.1:9379` 저장 중인 요청이 겹치지 않게 잠근다 |

## Kafka

```text
cd youtube_download
docker compose up -d
```

- Kafka `127.0.0.1:9092`
- Connect `127.0.0.1:8083`
- 앱이 기동하면서 커넥터 `youtube-download`를 등록한다.

## 실행

`yt-dlp`와 `ffmpeg`가 PATH에 있어야 파일이 생긴다.

```text
cd youtube_download
.\gradlew.bat bootRun
```

API는 `http://127.0.0.1:8080` 이다.

```text
cd youtube_download/frontend
pnpm install
pnpm dev
```

화면은 `http://127.0.0.1:3000` 이다. `/api`는 8080으로 넘긴다. <br/>
저장이 끝나면 목록의 `MinIO에서 받기`가 15분짜리 서명 주소다.

## 패키지

읽고 쓰는 유스케이스는 함수다. 프로세스, DB, 검색, 객체 저장, 잠금, 예외는 `global`이다.

| 패키지                 | 역할                                                                       |
| ---------------------- | -------------------------------------------------------------------------- |
| `video.save`           | 주소 붙여 넣기 `POST /api/downloads`                                       |
| `video.read` | 페이지 목록, 저장본 검색 `GET /api/downloads/saved`, 비슷한 저장, 추천 5개 `GET /api/downloads/recommendations` |
| `video.react` | 좋아요, 싫어요, 조회 |
| `global.embedding`     | 해시 임베딩, `bbq_hnsw` 매핑, 코사인, 9.5 버전 확인                        |
| `video`                | 영상 ID, 상태, 스냅샷                                                      |
| `global.persistence`   | MySQL 원장                                                                 |
| `global.cdc`           | Debezium 메시지, 파일 저장, 목록 반영                                      |
| `global.elasticsearch` | Elasticsearch 9 읽기 모델                                                  |
| `global.minio`         | 파일 업로드와 서명 주소                                                    |
| `global.redis`         | 같은 요청의 중복 저장 잠금                                                 |
| `global.ytdlp`         | `yt-dlp` 실행                                                              |
| `global.error`         | 한국어 오류                                                                |
| `global.env`           | 설정, 실행 진입                                                            |

`page`는 0부터 200까지, `size`는 1부터 50까지다. 기본 size는 10이다.

## 테스트

유튜브, Kafka, MySQL에는 붙지 않는다.

```text
cd youtube_download
.\gradlew.bat test
```

## 기술

- Kotlin 2.4.21, Java 17, Spring Boot 4.1
- Next.js 16, TypeScript, 컴포넌트
- Kafka 3.9
