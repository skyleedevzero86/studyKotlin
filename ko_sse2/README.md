# RAG 강화 스트리밍 채팅 시스템

## 📖 프로젝트 개요

이 프로젝트는 **Spring Boot + Kotlin**으로 구현된 **RAG(Retrieval-Augmented Generation) 강화 스트리밍 채팅 시스템**입니다. 사용자가 업로드한 문서를 기반으로 AI가 실시간으로 스트리밍 방식으로 답변을 제공합니다.

## ✨ 주요 기능

### �� AI 채팅
- **실시간 스트리밍 응답**: SSE(Server-Sent Events)를 통한 실시간 메시지 전송
- **RAG 모드**: 업로드된 문서를 기반으로 한 지식베이스 답변
- **일반 모드**: 일반적인 AI 대화 모드
- **스트리밍 시뮬레이션**: 청크 단위로 응답을 분할하여 자연스러운 타이핑 효과

### 📄 문서 처리
- **다양한 형식 지원**: PDF, TXT, MD, DOCX 등 Apache Tika를 통한 다중 형식 지원
- **텍스트 분할**: 문장, 단락, 커스텀 방식의 텍스트 청킹
- **벡터 저장소**: Redis를 활용한 문서 임베딩 및 유사도 검색

### ��️ 아키텍처
- **도메인 주도 설계(DDD)**: 계층별 명확한 책임 분리
- **함수형 프로그래밍**: Either, Option, Result 모나드를 활용한 에러 처리
- **전략 패턴**: 다양한 텍스트 분할 전략 지원
- **이벤트 기반**: 도메인 이벤트를 통한 비동기 처리

## 🛠️ 기술 스택

### Backend
- **언어**: Kotlin 1.9.25
- **프레임워크**: Spring Boot 3.5.4
- **AI**: Spring AI 0.8.1
- **데이터베이스**: Redis (벡터 저장소)
- **문서 처리**: Apache Tika
- **빌드 도구**: Gradle (Kotlin DSL)

### Frontend
- **HTML5 + CSS3 + JavaScript**
- **SSE(Server-Sent Events)**: 실시간 스트리밍
- **반응형 디자인**: 모바일 친화적 UI

## 📁 프로젝트 구조

```
src/main/kotlin/com/sleekydz86/rag/
├── application/                    # 애플리케이션 계층
│   ├── event/                     # 도메인 이벤트 처리
│   │   ├── DomainEventHandlers.kt
│   │   └── DomainEventPublisher.kt
│   └── service/                   # 비즈니스 로직
│       ├── ChatService.kt
│       ├── ChatServiceImpl.kt
│       ├── DocumentService.kt
│       ├── DocumentServiceImpl.kt
│       ├── EitherDocumentService.kt
│       ├── EitherDocumentServiceImpl.kt
│       └── ReactiveDocumentProcessor.kt
├── common/                        # 공통 유틸리티
│   └── functional/                # 함수형 프로그래밍
│       ├── extension/             # 확장 함수
│       │   └── Extensions.kt
│       ├── monad/                 # 모나드 구현
│       │   ├── Monad.kt
│       │   ├── Option.kt
│       │   └── Result.kt
│       └── validation/            # 검증 로직
│           ├── ChatEntityValidator.kt
│           └── Validators.kt
├── config/                        # 설정
│   ├── ApplicationConfig.kt
│   ├── ProxyConfig.kt
│   └── WebConfig.kt
├── domain/                        # 도메인 계층
│   ├── event/                     # 도메인 이벤트
│   │   └── DomainEvent.kt
│   └── model/                     # 도메인 모델
│       └── ChatEntity.kt
├── infrastructure/                # 인프라 계층
│   ├── cache/                     # 캐시 전략
│   │   ├── CacheStrategy.kt
│   │   └── RedisCacheStrategy.kt
│   ├── external/                  # 외부 시스템 연동
│   │   ├── SSEServer.kt
│   │   └── sse/
│   │       └── SSEMsgType.kt
│   └── persistence/               # 데이터 영속성
│       └── redis/
│           └── ApplicationProperties.kt
├── presentation/                  # 프레젠테이션 계층
│   ├── controller/                # REST 컨트롤러
│   │   ├── ChatController.kt
│   │   ├── RagController.kt
│   │   └── SseController.kt
│   └── dto/                       # 데이터 전송 객체
│       └── LeeResult.kt
└── shared/                        # 공유 컴포넌트
├── factory/                   # 팩토리 패턴
│   └── TextSplitterFactory.kt
└── strategy/                  # 전략 패턴
├── CustomTextSplitter.kt
├── ParagraphTextSplitter.kt
├── SentenceTextSplitter.kt
└── TextSplitStrategy.kt
```

## �� 시작하기

### 사전 요구사항
- Java 21
- Redis Server
- OpenAI API Key

### 1. 환경 설정

#### Redis 설정
```bash
# Redis 서버 시작 (포트: 9379)
redis-server --port 9379 --requirepass 123456
```

#### 환경 변수 설정
```bash
export OPENAI_API_KEY="your-openai-api-key-here"
```

### 2. 애플리케이션 실행

```bash
# 프로젝트 클론
git clone <repository-url>
cd ko_sse2

# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

### 3. 접속
- **웹 인터페이스**: http://localhost:8080/api
- **API 엔드포인트**: http://localhost:8080/api/chat/send

## �� API 엔드포인트

### 채팅
- `POST /api/chat/send` - 메시지 전송 및 스트리밍 응답

### 문서 업로드
- `POST /api/rag/upload` - 문서 업로드 및 벡터 저장소에 저장

### SSE 연결
- `GET /api/sse/connect?userId={userId}` - 실시간 스트리밍 연결

## �� 사용법

### 1. 문서 업로드
1. 웹 인터페이스에서 �� 버튼 클릭
2. 지원 형식의 문서 선택 (PDF, TXT, MD, DOCX)
3. 업로드 완료 메시지 확인

### 2. 채팅 시작
1. **지식베이스 모드**: 토글 스위치 ON → 업로드된 문서 기반 답변
2. **일반 모드**: 토글 스위치 OFF → 일반 AI 대화
3. 메시지 입력 후 전송

### 3. 실시간 응답
- SSE를 통한 실시간 스트리밍 응답
- 청크 단위로 분할된 자연스러운 타이핑 효과
- 연결 상태 실시간 모니터링

## 🔧 설정

### application.yml 주요 설정

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 9379
      password: 123456
  
  ai:
    vectorstore:
      redis:
        initialize-schema: true
        index-name: lee-vectorstore
        prefix: "lee:"
    
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-3.5-turbo
        temperature: 0.7
        max-tokens: 1000

server:
  port: 8080
  servlet:
    context-path: /api
```

## ��️ 아키텍처 패턴

### 1. 계층형 아키텍처
- **Presentation Layer**: REST API, SSE 처리
- **Application Layer**: 비즈니스 로직, 이벤트 처리
- **Domain Layer**: 도메인 모델, 이벤트
- **Infrastructure Layer**: 외부 시스템 연동, 데이터 영속성

### 2. 함수형 프로그래밍
- **Either**: 에러 처리와 성공 케이스 분리
- **Option**: null 안전성 보장
- **Result**: 예외 처리와 결과 분리

### 3. 전략 패턴
- **TextSplitStrategy**: 다양한 텍스트 분할 전략
- **CacheStrategy**: 캐시 구현 전략

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew integrationTest
```

## 📝 개발 가이드

### 새로운 텍스트 분할 전략 추가
1. `TextSplitStrategy` 인터페이스 구현
2. `TextSplitterFactory`에 새로운 타입 추가
3. Spring Bean으로 등록

### 새로운 AI 모델 추가
1. Spring AI 설정에 모델 추가
2. `ChatServiceImpl`에서 모델 선택 로직 구현

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 Apache License 2.0 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 👥 팀

- **개발자**: sleekydz86
- **프로젝트**: RAG 강화 스트리밍 채팅 시스템


