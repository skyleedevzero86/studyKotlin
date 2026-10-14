# youtube_download_msa

`youtube_download`를 게이트웨이와 서비스로 나눈 학습용 구성이다. <br/>
기존 모놀리스의 데이터베이스, 인덱스, 버킷, 커넥터는 그대로 둔다. <br/>
화면은 게이트웨이만 호출한다.

## 서비스

| 서비스       | 포트 | 역할                                                                                    |
| ------------ | ---- | --------------------------------------------------------------------------------------- |
| gateway      | 8090 | `/api/downloads`를 다운로드 원장 또는 검색으로 나눈다. 내부 API는 밖으로 열지 않는다    |
| download-api | 8091 | MySQL 원장, Redis 잠금, Debezium 커넥터 등록. 붙여 넣기와 상태 변경                     |
| media-worker | 8092 | `REQUESTED` 이벤트만 받아 `yt-dlp`로 파일을 받고 MinIO에 올린다                         |
| search-api   | 8093 | 같은 변경을 Elasticsearch에 반영한다. 목록, 저장본 검색, 벡터 검색, 추천 5개, 서명 주소 |
| frontend     | 3001 | Next.js. `/api`는 8090으로 넘긴다                                                       |

Kafka와 Connect는 `youtube_download`의 compose를 그대로 쓴다. 커넥터 이름만 `youtube-download-msa`다.

## 이 프로젝트가 쓰는 이름

| 구성                          | 값                                                     |
| ----------------------------- | ------------------------------------------------------ |
| MySQL                         | `127.0.0.1:3306` 데이터베이스 `youtube_download_msa`   |
| Elasticsearch 9.5             | `http://127.0.0.1:9201` 인덱스 `youtube-downloads-msa` |
| MinIO                         | 버킷 `youtube-download-msa`                            |
| Redis                         | `127.0.0.1:9379` 키 `youtubemsa:download:{id}`         |
| 토픽                          | `youtubemsa.download.events`                           |
| DLQ                           | `youtubemsa.download.events.DLT`                       |
| Debezium `database.server.id` | `85721`                                                |

## 실행

데이터베이스를 한 번 만든다. 모놀리스 데이터베이스는 지우지 않는다.

```text
CREATE DATABASE IF NOT EXISTS youtube_download_msa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON youtube_download_msa.* TO 'finsight'@'%';
```

```text
cd youtube_download_msa
.\gradlew.bat :gateway:bootRun
.\gradlew.bat :download-api:bootRun
.\gradlew.bat :media-worker:bootRun
.\gradlew.bat :search-api:bootRun
```

`yt-dlp`와 `ffmpeg`가 PATH에 있어야 파일이 생긴다.

```text
cd youtube_download_msa/frontend
pnpm install
pnpm dev
```

화면은 `http://127.0.0.1:3001` 이다.

## 요청이 가는 곳

| 요청                                                | 서비스                                           |
| --------------------------------------------------- | ------------------------------------------------ |
| `POST /api/downloads`                               | download-api                                     |
| `GET /api/downloads`                                | search-api                                       |
| `GET /api/downloads/saved`                          | search-api                                       |
| `POST /api/downloads/search`                        | search-api                                       |
| `GET /api/downloads/recommendations`                | search-api                                       |
| `POST /api/downloads/{id}/likes` `dislikes` `views` | search-api가 원장에 반영한 뒤 서명 주소를 붙인다 |

미디어 워커와 검색 서비스는 서로 다른 Kafka 컨슈머 그룹이라 같은 변경을 각자 처리한다. <br/>
검색 서비스는 MySQL을 쓰지 않고, 벡터가 필요하면 download-api에서 행을 읽는다.
