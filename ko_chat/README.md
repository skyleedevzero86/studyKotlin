<img width="1231" height="689" alt="image" src="https://github.com/user-attachments/assets/e123df7d-54f8-4e70-96ab-92f2e713e0dd" />

<br/>

# 코틀린기반의 미니몰 채팅 사이트 Ko_Chat!

Kotlin + Spring Boot 백엔드와 Vue 3 프론트엔드로 구성된 **실시간 채팅·화상 통화** 프로젝트입니다.

---

## 목차

- [왜 만들었는가](#왜-만들었는가)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [ERD](#erd)
- [프로젝트 흐름](#프로젝트-흐름)
- [디렉토리 구조](#디렉토리-구조)
- [실행 방법](#실행-방법)
- [환경 설정](#환경-설정)
- [API · WebSocket 요약](#api--websocket-요약)
- [Kafka 후처리 아키텍처](#kafka-후처리-아키텍처)
- [포트 정리](#포트-정리)

---

## 왜 만들었는가

이 프로젝트는 **Kotlin/Spring 기반 실시간 서비스**를 end-to-end로 경험하기 위해 만들었습니다.

| 목표                       | 설명                                                     |
| -------------------------- | -------------------------------------------------------- |
| **헥사고날 아키텍처 실습** | 도메인·포트와 어댑터 분리                                |
| **실시간 채팅**            | WebSocket + Redis Pub/Sub으로 다중 인스턴스 브로드캐스트 |
| **WebRTC 화상**            | SRS 미디어 서버 + 별도 시그널링 WebSocket                |
| **첨부파일·링크**          | MinIO 객체 저장, Milvus 벡터 인덱스                      |
| **운영형 사용자 관리**     | 가입 승인, 정지, 비밀번호 정책, 관리자 SSE               |
| **관리자 운영**            | 채팅방 강제 퇴장, 메시지 통계·차트·Excel/PDF보내기       |
| **소셜 + 채팅 통합**       | 친구·차단·초대·강퇴·비공개 방 등 실서비스에 가까운 흐름  |

단순 CRUD 데모가 아니라, **인증 → 관계 → 방 생성 → 실시간 메시지 → 첨부파일 → WebRTC**까지 한 앱 안에서 연결되도록 설계했습니다.

---

## 주요 기능

### 사용자 · 인증

- 회원가입 가입 후 **관리자 승인** 필요, `PENDING` → `ACTIVE`
- JWT 로그인
- 토큰 만료 검증 및 401 응답 시 자동 로그아웃·재로그인 유도
- 프로필 수정, 탈퇴, 비밀번호 변경 (30일 만료·실패 잠금 정책)
- 부트스트랩 관리자 계정 자동 생성

### 관리자

| 화면        | 경로                | 기능                                                                               |
| ----------- | ------------------- | ---------------------------------------------------------------------------------- |
| 사용자 관리 | `/admin/users`      | 목록 조회, SSE 실시간 스트림, 승인·정지·활성화·복구·잠금 해제·역할 변경·삭제       |
| 채팅방 관리 | `/admin/chat-rooms` | 활성 채팅방 목록, 멤버 조회, **강제 퇴장**, 메시지 감사 조회                       |
| 통계        | `/admin/statistics` | 시간대별·메시지 유형별·채팅방 유형별 집계, **Chart.js 막대 차트**, Excel/PDF보내기 |

- 사용자 목록: 민감 정보 AES 암호화 payload
- 통계 필터: 검색기간, 채팅방 유형, 메시지 유형
- 관리자 화면 간 네비게이션: 채팅 · 사용자 관리 · 채팅방 관리 · 통계 · 내 정보

### 채팅

| 구분        | 내용                                                             |
| ----------- | ---------------------------------------------------------------- |
| 방 종류     | `DIRECT`(1:1), `GROUP`, `CHANNEL`                                |
| 미디어 모드 | `일반 채팅`, `화상대화 2~6명`                                    |
| 공개 설정   | 공개 방 / 비공개 + 비밀번호                                      |
| 참여        | 초대 수락, 오픈채팅 탐색, 친구 목록 1:1, REST `POST .../members` |
| 검색        | 참여 중인 방: 제목·설명·**메시지 내용**·상대 이름으로 검색       |
| 발견        | 공개 그룹/채널 탐색                                              |
| 관리        | 방 설정, 정원 변경, 강퇴·재입장 차단, 나가기                     |
| 실시간      | WebSocket 메시지 송수신, 읽음 처리, 시스템 메시지                |
| 후처리      | Kafka 이벤트                                                     |

### 메시지 · 첨부파일

| 타입     | 설명                          |
| -------- | ----------------------------- |
| `TEXT`   | 일반 텍스트                   |
| `IMAGE`  | 이미지 미리보기               |
| `FILE`   | 파일 카드 + 다운로드 링크     |
| `LINK`   | URL 링크 미리보기             |
| `SYSTEM` | 입장·퇴장·강퇴 등 시스템 알림 |

- 파일 업로드: `POST /chat-rooms/{id}/attachments` → MinIO 저장 → WebSocket `SEND_MESSAGE`로 전송
- 링크 미리보기: `POST /chat-rooms/link-preview`
- 첨부 메타데이터는 `messages.metadata` JSON 컬럼에 저장
- Milvus에 첨부파일 메타 벡터 등록

### WebRTC 화상

- 방 생성 시 **일반 채팅 / WebRTC 화상** 선택
- 참여 방식: **음성만** / **카메라+음성** / 시청 전용
- 마이크 음소거, 카메라 on/off, 화면 공유
- 강퇴·나가기 시 WebRTC 세션 연동 해제

### 친구 · 차단

- 친구 요청 / 수락 / 거절
- 사용자 차단 및 차단 이력
- 친구 목록에서 1:1 채팅 바로 시작
- 채팅 초대와 친구 요청 통합 알림 UI

### 프론트엔드 UI

메신저 스타일 3단 레이아웃

| 영역          | 컴포넌트              | 역할                                      |
| ------------- | --------------------- | ----------------------------------------- |
| 좌측 내비     | `sleekydz86-nav-rail` | 친구 / 채팅 / 더보기 탭, 안읽음·요청 뱃지 |
| 목록 패널     | `FriendListPanel`     | 친구 검색·추가, 1:1 채팅                  |
|               | `ChatRoomList`        | 채팅 목록, 전체/안읽음 필터, 방 생성·검색 |
|               | `MorePanel`           | 프로필 이동, 차단 목록, **관리자 메뉴**   |
| 관리자        | `AdminUsersView`      | 사용자 승인·정지·역할 관리                |
|               | `AdminChatRoomsView`  | 채팅방·멤버 조회, 강제 퇴장               |
|               | `AdminStatisticsView` | 통계 차트·표·엑셀/PDF보내기               |
|               | `StatisticsBarChart`  | Chart.js 막대·누적 차트                   |
|               | `OpenChatSearchPanel` | 오픈채팅·1:1 사용자 탐색                  |
| 메인 패널     | `ChatWindow`          | 말풍선 채팅, 멤버·설정, 파일 업로드       |
|               | `WebRtcPanel`         | WebRTC 화상                               |
| 메시지 렌더링 | `ChatMessageContent`  | TEXT/IMAGE/FILE/LINK 타입별 표시          |

---

## 기술 스택

### Backend

| 항목          | 기술                               |
| ------------- | ---------------------------------- |
| 언어          | Kotlin 2.2, Java 21                |
| 프레임워크    | Spring Boot 4.0, Spring Security 7 |
| DB            | MySQL                              |
| 캐시 · 메시징 | Redis                              |
| 이벤트 스트림 | Apache Kafka                       |
| 객체 저장소   | MinIO                              |
| 벡터 DB       | Milvus                             |
| 인증          | JWT , BCrypt                       |
| API 문서      | springdoc-openapi                  |
| 실시간        | Spring WebSocket                   |
| 미디어        | SRS 5                              |
| 통계보내기    | Apache POI , OpenPDF               |

### Frontend

| 항목       | 기술                                                           |
| ---------- | -------------------------------------------------------------- |
| 프레임워크 | Vue 3                                                          |
| 빌드       | Vite 8, TypeScript 6                                           |
| 라우팅     | Vue Router 5                                                   |
| 상태       | Composables                                                    |
| HTTP       | `fetch` 래퍼                                                   |
| 차트       | Chart.js 4                                                     |
| 타입       | `types/chat/`, `types/user/`, `types/statistics/` 등 모듈 분리 |

---

## 시스템 아키텍처

### 전체 구성

```mermaid
flowchart TB
    subgraph Client["브라우저 (Vue 3 :3000)"]
        UI[ChatView / ChatWindow / WebRtcPanel]
        ChatWS[Chat WebSocket Client]
        MediaWS[WebMedia WebSocket Client]
        SRSClient[SRS publish/play HTTP]
    end

    subgraph Backend["Spring Boot (:8080)"]
        REST[REST Controllers]
        Attach[ChatAttachmentController]
        ChatHandler[ChatWebSocketHandler]
        MediaHandler[WebMediaWebSocketHandler]
        Domain[Domain Services]
        JPA[JPA Repositories]
        RedisBroker[RedisMessageBroker]
        MinioSvc[MinioStorageService]
        MilvusSvc[MilvusAttachmentIndexService]
    end

    subgraph Infra["인프라"]
        MySQL[(MySQL)]
        Redis[(Redis)]
        MinIO[(MinIO :9000)]
        Milvus[(Milvus :19530)]
        SRS[SRS :1985 / :8000/udp]
    end

    UI --> REST
    UI --> ChatWS
    UI --> MediaWS
    REST --> Attach
    Attach --> MinioSvc
    Attach --> Domain
    MediaWS --> MediaHandler
    ChatWS --> ChatHandler
    REST --> Domain
    ChatHandler --> Domain
    MediaHandler --> Domain
    Domain --> JPA
    Domain --> RedisBroker
    Domain --> MilvusSvc
    JPA --> MySQL
    RedisBroker --> Redis
    ChatHandler --> RedisBroker
    MinioSvc --> MinIO
    MilvusSvc --> Milvus
    SRSClient --> SRS
    MediaHandler -.시그널링.-> MediaWS
```

### 백엔드 아키텍처

```
com.kochat
├── domain/              # 도메인 모델, messaging 이벤트 타입
├── adapter/
│   ├── inbound/         # REST, WebSocket, Kafka Consumer
│   └── outbound/        # JPA, Redis, MinIO, Milvus, Outbox
└── global/              # Security, JWT, Outbox Relay, 애플리케이션 서비스
```

| 레이어                           | 역할                                           |
| -------------------------------- | ---------------------------------------------- |
| **domain**                       | 비즈니스 규칙, `ChatEventType`, 이벤트 payload |
| **adapter/inbound**              | REST·WebSocket·**Kafka Consumer**              |
| **adapter/outbound**             | DB·Redis·MinIO·Milvus·**Outbox**               |
| **global/application/messaging** | Outbox 저장, Relay, 멱등성 처리                |

### 실시간 채팅 vs WebRTC

|               | 일반 채팅               | WebRTC 화상            |
| ------------- | ----------------------- | ---------------------- |
| 시그널링      | `/api/v1/ws/chat`       | `/api/v1/ws/webmedia`  |
| 메시지·텍스트 | WebSocket + DB 저장     | 동일                   |
| 첨부파일      | MinIO + WebSocket       | 동일                   |
| 영상·음성     | 없음                    | SRS `publish` / `play` |
| 방 관리       | 초대·강퇴·비밀번호·설정 | **동일 API·UI**        |

채팅 메시지는 **백엔드 WebSocket + Redis**로 처리하고, WebRTC **미디어 스트림만 SRS**가 중계합니다.

### 실시간 vs Kafka 후처리

| 구분          | 담당                      | 설명                                               |
| ------------- | ------------------------- | -------------------------------------------------- |
| 실시간 전달   | WebSocket + Redis Pub/Sub | 채팅 말풍선, 다중 인스턴스 브로드캐스트            |
| 후처리 이벤트 | Kafka                     | 감사 로그, 검색 인덱스, 첨부 후처리, Milvus 인덱싱 |

메시지 저장 트랜잭션 안에서 **Outbox**에 이벤트를 기록하고, 별도 Relay가 Kafka로 발행합니다. <br/>
Kafka가 잠시 중단되어도 DB에 이벤트가 남아 재발행할 수 있습니다.

```mermaid
sequenceDiagram
    participant WS as WebSocket Client
    participant TX as ChatMessageTxService
    participant DB as MySQL
    participant OB as Outbox Relay
    participant K as Kafka
    participant C as Consumers

    WS->>TX: SEND_MESSAGE
    TX->>DB: messages + outbox_events (동일 TX)
    TX-->>WS: afterCommit → Redis/WebSocket 실시간 전달

    loop 3초마다
        OB->>DB: PENDING outbox 조회
        OB->>K: publish (key=roomId)
        OB->>DB: PUBLISHED 처리
    end

    K->>C: chat.message.events
    Note over C: audit-consumer, search-index-consumer
    K->>C: chat.attachment.events
    Note over C: attachment-consumer, milvus-index-consumer
```

---

## ERD

```mermaid
erDiagram
    users ||--o{ chat_rooms : creates
    users ||--o{ chat_room_members : joins
    chat_rooms ||--o{ chat_room_members : has
    users ||--o{ messages : sends
    chat_rooms ||--o{ messages : contains
    messages ||--o| message_attachments : has
    users ||--o{ chat_room_invitations : invites
    chat_rooms ||--o{ chat_room_invitations : has
    users ||--o{ chat_room_bans : banned
    chat_rooms ||--o{ chat_room_bans : has
    users ||--o{ user_friends : owns
    users ||--o{ user_friend_requests : requests
    users ||--o{ user_blocks : blocks

    users {
        bigint id PK
        string username UK
        string password
        string display_name
        enum role
        enum status
        datetime created_at
        datetime password_changed_at
    }

    chat_rooms {
        bigint id PK
        string name
        enum type
        enum media_mode
        boolean is_private
        string password_hash
        int max_members
        bigint created_by FK
    }

    chat_room_members {
        bigint id PK
        bigint chat_room_id FK
        bigint user_id FK
        enum role
        boolean is_active
        bigint last_read_message_id
    }

    messages {
        bigint id PK
        bigint chat_room_id FK
        bigint sender_id FK
        enum type
        text content
        text metadata
        bigint sequence_number
    }

    message_attachments {
        bigint id PK
        bigint message_id FK
        bigint chat_room_id FK
        string object_key
        string file_name
        string mime_type
        bigint size
        boolean milvus_indexed
    }

    chat_room_invitations {
        bigint id PK
        bigint chat_room_id FK
        bigint inviter_id FK
        bigint invitee_id FK
        enum status
    }

    chat_room_bans {
        bigint id PK
        bigint chat_room_id FK
        bigint user_id FK
        bigint banned_by FK
        boolean is_active
    }

    user_friends {
        bigint id PK
        bigint owner_id FK
        bigint friend_id FK
        boolean is_active
    }

    user_friend_requests {
        bigint id PK
        bigint requester_id FK
        bigint recipient_id FK
        enum status
    }

    user_blocks {
        bigint id PK
        bigint blocker_id FK
        bigint blocked_id FK
        boolean is_active
    }
```

### 주요 테이블 설명

| 테이블                                  | 설명                                                                      |
| --------------------------------------- | ------------------------------------------------------------------------- |
| `users`                                 | 계정, 역할, 상태                                                          |
| `chat_rooms`                            | 방 메타. `media_mode`: `TEXT` \| `WEBRTC`, `is_private` + `password_hash` |
| `chat_room_members`                     | 멤버십, 역할, 읽음 위치                                                   |
| `messages`                              | `TEXT`/`IMAGE`/`FILE`/`LINK`/`SYSTEM`, `metadata` JSON, 시퀀스 번호       |
| `message_attachments`                   | MinIO `object_key`, Milvus 인덱싱 여부                                    |
| `chat_room_invitations`                 | 초대·수락·거절                                                            |
| `chat_room_bans`                        | 강퇴 후 재입장 차단                                                       |
| `user_friends` / `user_friend_requests` | 친구 관계                                                                 |
| `user_blocks`                           | 차단                                                                      |

---

## 프로젝트 흐름

### 1. 회원가입 · 로그인

```mermaid
sequenceDiagram
    actor U as 사용자
    participant F as Frontend
    participant B as Backend
    participant A as Admin

    U->>F: 회원가입 (/join)
    F->>B: POST /api/v1/join
    B-->>F: PENDING 상태 생성
    A->>B: POST /admin/users/{username}/approve
    U->>F: 로그인 (/login)
    F->>B: POST /api/v1/login
    B-->>F: accessToken (JWT)
    F->>F: localStorage 저장
    F->>U: 채팅 홈 (/) 이동
```

### 2. 채팅방 생성 · 참여 · 메시지

```mermaid
sequenceDiagram
    actor U as 사용자
    participant F as Frontend
    participant B as Backend
    participant WS as Chat WebSocket
    participant R as Redis

    U->>F: 방 생성 (일반/WebRTC, 공개/비공개)
    F->>B: POST /api/v1/chat-rooms
    B-->>F: 방 정보 (생성자 자동 입장)

    alt 다른 사용자 참여
        U->>F: 오픈채팅 탐색 / 초대 수락 / 친구 1:1
        F->>B: POST join / accept invitation / direct
    end

    U->>F: 방 선택
    F->>B: GET /messages (히스토리)
    F->>WS: connect ?token=JWT
    U->>F: 메시지 입력
    F->>WS: SEND_MESSAGE
    WS->>B: 저장 + Redis publish
    B->>R: room:{id} 채널
    R->>WS: 다른 인스턴스/세션에 전달
    WS-->>F: CHAT_MESSAGE
```

### 3. 첨부파일 업로드

```mermaid
sequenceDiagram
    actor U as 사용자
    participant F as Frontend
    participant B as Backend
    participant M as MinIO
    participant WS as Chat WebSocket

    U->>F: 파일 선택 (ChatWindow)
    F->>B: POST /chat-rooms/{id}/attachments (multipart)
    B->>M: PutObject
    M-->>B: objectKey + presigned URL
    B-->>F: messageType + metadata
    F->>WS: SEND_MESSAGE (IMAGE/FILE + metadata)
    WS-->>F: CHAT_MESSAGE (방 전체 브로드캐스트)
```

### 4. WebRTC 화상 (WEBRTC 방)

```mermaid
sequenceDiagram
    actor U as 사용자
    participant F as Frontend
    participant M as WebMedia WS
    participant S as SRS

    U->>F: WebRTC 방 입장
    F->>M: JoinRequest
    M-->>F: JoinResponse (apiUrl, streamUrl, 참가자 목록)

    alt 음성만 / 카메라+음성
        U->>F: 참여하기 → 방식 선택
        F->>F: getUserMedia (audio 또는 audio+video)
        F->>S: POST /rtc/v1/publish (SDP)
        F->>M: UserPublishedChangeReport published=true
    end

    Note over F,S: 다른 참가자 영상 수신
    F->>S: POST /rtc/v1/play (상대 userId 스트림)

    alt 강퇴
        F->>B: POST kick (REST)
        B->>M: UserKickedEvent
        F->>F: 세션 종료
    end
```

### 5. 프론트엔드 화면 흐름

```
/login, /join              → 비로그인
/                          → ChatView
/welcome                   → 기능 허브
/profile                   → 프로필
/admin/users               → 관리자 · 사용자 관리
/admin/chat-rooms          → 관리자 · 채팅방 관리
/admin/statistics          → 관리자 · 통계
/error, /*                 → 에러 / 404 페이지
```

`ChatView` 내부 탭:

```
친구 탭   → FriendListPanel
채팅 탭   → ChatRoomList
더보기 탭 → MorePanel
```

---

## 디렉토리 구조

```
ko_chat/
├── README.md                 # 이 문서
├── docker-compose.srs.yml    # SRS WebRTC 미디어 서버
├── backend/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/kochat/
│       ├── adapter/
│       │   ├── inbound/
│       │   │   ├── web/admin/          # 관리자 REST
│       │   │   ├── web/chat/           # ChatController, ChatAttachmentController
│       │   │   ├── kafka/consumer/     # audit, search-index, attachment, milvus
│       │   │   └── websocket/          # Chat WS, WebMedia WS
│       │   └── outbound/
│       │       ├── persistence/
│       │       │   ├── chat/           # JPA·ChatServiceImpl
│       │       │   ├── messaging/      # audit, search index, processed_events
│       │       │   └── outbox/         # outbox_events
│       │       └── storage/            # MinioStorageService, MilvusAttachmentIndexService
│       ├── domain/messaging/           # ChatEventType, 이벤트 모델
│       └── global/
│           ├── application/
│           │   ├── admin/              # 통계 조회·보내기
│           │   ├── chat/               # ChatMessageTxService, Dispatch
│           │   └── messaging/          # OutboxEventService, OutboxRelayService
│           └── config/                 # KafkaProperties, Minio, Milvus, Security
└── frontend/
    ├── package.json
    ├── vite.config.ts
    └── src/
        ├── api/                        # chatApi, userApi, authApi, http
        ├── components/
        │   ├── ChatRoomList.vue        # 채팅 목록·방 생성·검색
        │   ├── ChatWindow.vue          # 채팅창·설정·첨부 업로드
        │   ├── ChatMessageContent.vue  # 메시지 타입별 렌더링
        │   ├── FriendListPanel.vue     # 친구 목록·1:1
        │   ├── OpenChatSearchPanel.vue # 오픈채팅·사용자 탐색
        │   ├── MorePanel.vue           # 더보기·차단
        │   └── WebRtcPanel.vue         # WebRTC 화상
        ├── composables/                # useAuth, useWebSocket, useWebMedia, …
        ├── types/chat/                 # chat-room, message, enums, websocket, …
        ├── views/                      # ChatView, LoginView, AdminUsersView, …
        ├── lib/webmedia/               # SRS publish/play 클라이언트
        └── router/                     # 라우팅·가드
```

---

## 실행 방법

### 사전 요구사항

- **Java 21**
- **Node.js**
- **MySQL** `localhost:3306`, DB `finsight`
- **Redis** `localhost:9379`
- **MinIO** `localhost:9000`
- **Milvus** `localhost:19530`
- **Kafka** `localhost:9092`
- **Docker** — SRS

### 1. MySQL · Redis

`backend/src/main/resources/application.properties` 기준

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finsight?...
spring.datasource.username=finsight
spring.datasource.password=root123
spring.data.redis.host=localhost
spring.data.redis.port=9379
```

DB·계정을 먼저 생성한 뒤 백엔드를 실행하세요.

### 2. MinIO

```powershell
docker run -d --name minio `
  -p 9000:9000 -p 9001:9001 `
  -e MINIO_ROOT_USER=minioadmin `
  -e MINIO_ROOT_PASSWORD=minioadmin `
  minio/minio server /data --console-address ":9001"
```

`application.properties` 기본값과 일치합니다.
첨부파일 없이 텍스트 채팅만 사용할 경우 `app.minio.enabled=false`로 비활성화할 수 있습니다.

### 3. Milvus

Milvus Standalone을 로컬에 띄운 뒤 `app.milvus.host=localhost`, `app.milvus.port=19530`으로 연결합니다. <br/>
Milvus가 없어도 앱은 기동되며, 첨부파일 벡터 등록만 건너뜁니다.<br/>

```properties
app.milvus.enabled=false   # Milvus 없이 실행하려면
```

### 4. Kafka

RAG Docker Compose의 `rag-kafka` 컨테이너를 사용합니다.<br/>
호스트에서 Spring Boot가 접속하려면 **advertised listener**에 `localhost`가 포함되어야 합니다.

```yaml
KAFKA_CFG_LISTENERS: PLAINTEXT://:9092,PLAINTEXT_HOST://:29092,CONTROLLER://:9093
KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
```

```properties
app.kafka.enabled=true
app.kafka.bootstrap-servers=localhost:29092
```

Kafka 없이 실행: `app.kafka.enabled=false`

### 5. SRS

```powershell
# 프로젝트 루트
docker compose -f docker-compose.srs.yml up -d
```

| 포트     | 용도          |
| -------- | ------------- |
| 1985     | SRS HTTP API  |
| 8088     | SRS 내장 HTTP |
| 8000/udp | WebRTC 미디어 |

> WebRTC publish/play는 **1985**만 사용합니다.
> `docker-compose.srs.yml`은 SRS 컨테이너 **8088**로 매핑해 Spring Boot 기본 포트와 충돌하지 않습니다.

### 6. 백엔드

```powershell
cd backend
.\gradlew.bat bootRun
```

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

### 7. 프론트엔드

```powershell
cd frontend
npm install   # 또는 pnpm install
npm run dev
```

- UI: http://localhost:3000
- `/api`, `/actuator`, WebSocket은 Vite가 `8080`으로 프록시
- SRS API는 `/srs` → `localhost:1985` 프록시

### 8. 최초 로그인

`application.properties` 기본값:

| 항목     | 값             |
| -------- | -------------- |
| 아이디   | `admin`        |
| 비밀번호 | `admin1234!@#` |

일반 사용자는 `/join` 가입 후 관리자 승인이 필요합니다.

---

## 환경 설정

### Backend

| 키                                   | 설명                          |
| ------------------------------------ | ----------------------------- |
| `jwt.secret`                         | JWT 서명 키                   |
| `jwt.access-token-expire-time`       | 토큰 만료                     |
| `app.admin.bootstrap.*`              | 시작 시 관리자 계정 생성      |
| `app.encryption.secret`              | 관리자 API 민감 데이터 AES 키 |
| `app.webmedia.api-url`               | SRS HTTP API                  |
| `app.webmedia.stream-url`            | WebRTC 스트림 URL             |
| `app.minio.enabled`                  | MinIO 사용 여부               |
| `app.minio.endpoint`                 | MinIO 엔드포인트              |
| `app.minio.access-key`               | MinIO 액세스 키               |
| `app.minio.secret-key`               | MinIO 시크릿 키               |
| `app.minio.bucket`                   | 버킷 이름                     |
| `app.milvus.enabled`                 | Milvus 사용 여부              |
| `app.milvus.host`                    | Milvus 호스트                 |
| `app.milvus.port`                    | Milvus 포트                   |
| `app.milvus.collection`              | 컬렉션 이름                   |
| `app.kafka.enabled`                  | Kafka 후처리 사용 여부        |
| `app.kafka.bootstrap-servers`        | Kafka broker 주소             |
| `app.kafka.topics.message-events`    | 메시지 이벤트 토픽            |
| `app.kafka.topics.attachment-events` | 첨부 이벤트 토픽              |
| `spring.servlet.multipart.*`         | 업로드 크기 제한              |

### Frontend

| 변수                     | 설명                         |
| ------------------------ | ---------------------------- |
| `VITE_API_BASE_URL`      | API 베이스                   |
| `VITE_ENCRYPTION_SECRET` | 관리자 사용자 목록 복호화 키 |

---

## Kafka 후처리 아키텍처

### 역할 분리

| 경로        | 기술                      | 용도                                               |
| ----------- | ------------------------- | -------------------------------------------------- |
| 실시간 전달 | WebSocket + Redis Pub/Sub | 채팅 말풍선, 멀티 인스턴스 동기화                  |
| 후처리      | Kafka + Outbox            | 감사 로그, 검색 인덱스, 첨부 후처리, Milvus 인덱싱 |

Kafka에는 **메시지 원문**이 아니라 **이벤트**만 발행합니다. <br/>
partition key는 `roomId`로 설정해 방 단위 순서를 유지합니다.

### 토픽

| 토픽                         | 이벤트                | Consumer                                       |
| ---------------------------- | --------------------- | ---------------------------------------------- |
| `chat.message.events`        | `CHAT_MESSAGE_SENT`   | `audit-consumer`, `search-index-consumer`      |
| `chat.attachment.events`     | `ATTACHMENT_UPLOADED` | `attachment-consumer`, `milvus-index-consumer` |
| `chat.message.events.dlq`    | 실패 이벤트           | 수동 재처리                                    |
| `chat.attachment.events.dlq` | 실패 이벤트           | 수동 재처리                                    |

### Outbox 패턴

```
@Transactional
1. messages 저장
2. outbox_events 저장
3. commit

OutboxRelayService (3초 주기)
4. PENDING outbox 조회
5. Kafka 발행 (key=roomId)
6. PUBLISHED 처리
```

### 멱등성 · DLQ

- `processed_events` 테이블에 unique로 중복 처리 방지
- Consumer 실패 시 3회 재시도 후 DLQ 토픽으로 이동

### 이벤트 예시

```json
{
  "eventId": "evt-001",
  "eventType": "CHAT_MESSAGE_SENT",
  "roomId": 10,
  "messageId": 123,
  "sequenceNumber": 104,
  "senderId": 5,
  "messageType": "TEXT",
  "createdAt": "2026-06-30T22:30:00"
}
```

```json
{
  "eventId": "evt-002",
  "eventType": "ATTACHMENT_UPLOADED",
  "roomId": 10,
  "messageId": 123,
  "attachmentId": 45,
  "objectKey": "chat/10/uuid-file.png",
  "fileName": "file.png",
  "mimeType": "image/png",
  "size": 1024,
  "createdAt": "2026-06-30T22:30:01"
}
```

---

## API · WebSocket 요약

### REST

| 영역            | 대표 경로                                                                                               |
| --------------- | ------------------------------------------------------------------------------------------------------- |
| 인증            | `POST /login`, `POST /join`                                                                             |
| 사용자          | `GET /user/me`, `PUT /user/profile`, `GET /users/search`                                                |
| 친구·차단       | `/users/friends`, `/users/friend-requests`, `/users/blocks`                                             |
| 채팅방          | `POST/GET /chat-rooms`, `POST /direct`, `PUT .../settings`, `POST .../kick`                             |
| 발견·참여       | `GET /chat-rooms/discover`, `/discover/recommended`, `/search`                                          |
| 메시지          | `GET /chat-rooms/{id}/messages`, `/messages/cursor`                                                     |
| 첨부            | `POST /chat-rooms/{id}/attachments`, `POST /chat-rooms/link-preview`, `GET /chat-rooms/files/url`       |
| 관리자 · 사용자 | `GET/POST /admin/users`, `GET /admin/users/stream`                                                      |
| 관리자 · 채팅방 | `GET /admin/chat-rooms`, `GET /{id}/members`, `POST /{id}/members/{userId}/kick`, `GET /{id}/messages`  |
| 관리자 · 통계   | `GET /admin/statistics/hourly`, `/message-types`, `/room-types`, 각 탭별 `/export/excel`, `/export/pdf` |

#### 관리자 통계 API

| 엔드포인트                                 | 설명                       | 주요 쿼리 파라미터                      |
| ------------------------------------------ | -------------------------- | --------------------------------------- |
| `GET /admin/statistics/hourly`             | 시간대 별 메시지 건수·비율 | `from`, `to`, `roomType`, `messageType` |
| `GET /admin/statistics/message-types`      | 메시지 유형별 년도별 집계  | `from`, `to`, `roomType`                |
| `GET /admin/statistics/room-types`         | 채팅방 유형별 일자별 집계  | `from`, `to`, `messageType`             |
| `GET /admin/statistics/{tab}/export/excel` | Excel보내기 필터           |
| `GET /admin/statistics/{tab}/export/pdf`   | PDF보내기                  | 동일 필터                               |

#### 채팅방 검색·발견 파라미터

| API         | 주요 파라미터                                      |
| ----------- | -------------------------------------------------- |
| `/search`   | `q` — 참여 중인 방 제목·설명·메시지·상대 이름 검색 |
| `/discover` | `q`, `roomType` , `includePrivate`, `sort`         |

### WebSocket

| URL                                        | 용도            |
| ------------------------------------------ | --------------- |
| `ws://host/api/v1/ws/chat?token={JWT}`     | 실시간 채팅     |
| `ws://host/api/v1/ws/webmedia?token={JWT}` | WebRTC 시그널링 |

채팅 클라이언트 → 서버:

```json
{
  "type": "SEND_MESSAGE",
  "chatRoomId": 1,
  "messageType": "TEXT",
  "content": "안녕하세요"
}
```

첨부파일은 REST 업로드 후 `messageType`과 `metadata`를 함께 전송합니다.

WebMedia: `JoinRequest`, `UserPublishedChangeReport` 등 ([`WebMediaMessageType`](backend/src/main/kotlin/com/kochat/adapter/inbound/websocket/webmedia/WebMediaMessageType.kt))

---

## 포트 정리

| 서비스                | 포트     | 비고                        |
| --------------------- | -------- | --------------------------- |
| Frontend (Vite)       | 3000     | 개발 서버                   |
| Backend (Spring Boot) | 8080     | REST + WS                   |
| MySQL                 | 3306     |                             |
| Redis                 | 9379     | 캐시 + Pub/Sub              |
| MinIO API             | 9000     | 첨부파일 업로드             |
| MinIO Console         | 9001     | 웹 관리 콘솔                |
| Milvus                | 19530    | 첨부 벡터 인덱스            |
| SRS HTTP API          | 1985     | WebRTC 시그널링 HTTP        |
| SRS 내장 HTTP         | 8088     | 컨테이너 8080 → 호스트 8088 |
| SRS WebRTC            | 8000/udp | 미디어                      |

## 라이선스 · 기여

학습·실습용 프로젝트입니다. 이슈·PR은 자유롭게 활용해 주세요.
