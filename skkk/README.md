

일반 사이트 시맨틱 검색. 벡터 검색 기반.
<br/>
<img width="649" height="258" alt="image" src="https://github.com/user-attachments/assets/803fcde9-c986-4e63-b962-dee92cd18d38" />

<br/>
---

## 프로젝트 소개

**skkk**는 웹 페이지를 수집해 벡터 임베딩으로 저장한 뒤, **키워드가 아닌 의미**로 검색할 수 있게 해 주는 웹 어플리케이션입니다.

- **수집**: 단일 URL, RSS/Atom 피드, Velog 프로필 리스트등에서 글 URL을 뽑아 본문을 크롤링하고, TEI(Qwen3-Embedding)로 임베딩한 뒤 ChromaDB에 저장합니다.
- **검색**: 검색어를 같은 모델로 임베딩하고, ChromaDB에서 유사도 기반으로 관련 문서를 찾아 반환합니다.
- **스택**: 백엔드는 Kotlin + Spring Boot, 프론트는 Next.js + TypeScript입니다. <br/>
인프라는 Docker로 TEI·ChromaDB를 띄워 사용합니다.

Velog 프로필 URL 하나로 해당 블로그 글을 일괄 수집한 뒤, 자연어 질의로 관련 글을 찾는 용도에 맞춰 구성되어 있습니다.

---

## 아키텍처

```
                    ┌─────────────────────────────────────────────────────────┐
                    │                    Next.js (Frontend) :3000              │
                    │  presentation → application → infrastructure   │
                    └───────────────────────────┬─────────────────────────────┘
                                                │ HTTP /api/*
                                                ▼
                    ┌─────────────────────────────────────────────────────────┐
                    │              Spring Boot (Backend)                 │
                    │  ui(controller) → application(use case) → domain(port)    │
                    │                    ↑                                      │
                    │                    │ infrastructure(adapter) 구현 주입    │
                    └───────────┬───────────────────────┬─────────────────────┘
                                │                       │
            POST /embed         │                       │  REST API v2
            (임베딩 벡터)        │                       │  (저장/검색)
                                ▼                       ▼
                    ┌───────────────────┐   ┌───────────────────┐
                    │  TEI :4444        │   │  ChromaDB :4444   │
                    │  Qwen3-Embedding  │   │  벡터 저장/유사도   │
                    │  0.6B (1024차원)   │   │  검색              │
                    └───────────────────┘   └───────────────────┘
```


---

## 사용 방법

**요구사항**: JDK 21, Node.js 20+, pnpm, Docker (TEI, ChromaDB)

### 1. 인프라

```bash
cd skkk
docker compose up -d
```

| 서비스   | 포트 | 용도               |
|----------|------|--------------------|
| ChromaDB | 4444 | 벡터 저장/검색     |
| TEI      | 4444 | 텍스트 → 임베딩    |

### 2. 백엔드

```bash
cd skkk/backend
./gradlew bootRun
```

API: `http://localhost:8080`

### 3. 프론트엔드

```bash
cd skkk/frontend
pnpm install
pnpm dev
```

UI: `http://localhost:3000`

### 4. 수집·검색

| 동작           | 방법 |
|----------------|------|
| 단일 URL 수집  | `POST /api/ingest/url` Body: `{ "url": "https://..." }` |
| Velog 프로필 수집 | `POST /api/ingest/list` Body: `{ "listUrl": "https://velog.io/@username/posts", "maxItems": 50 }` |
| RSS 피드 수집  | `POST /api/ingest/feed` Body: `{ "feedUrl": "...", "maxPosts": 50 }` |
| 검색           | `GET /api/search?q=검색어&topK=5` 또는 UI `http://localhost:3000` |
