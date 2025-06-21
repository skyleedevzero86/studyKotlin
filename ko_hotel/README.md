# Ko-Hotel AI 챗봇

이 프로젝트는 Spring AI와 RAG(Retrieval-Augmented Generation) 아키텍처를 기반으로 하는 AI 호텔 안내 챗봇입니다. 사용자의 호텔 관련 질문에 실시간으로 답변하며, 자연스러운 대화 경험을 제공합니다.

## ARCHITECTURE

프로젝트의 아키텍처와 데이터 흐름은 아래 다이어그램과 같습니다.

![Ko-Hotel Architecture](https://user-images.githubusercontent.com/12185848/289091593-3be45615-5658-412f-98f6-3c06d2a715f0.png)

1.  **사용자 질문**: 사용자가 웹 UI(Thymeleaf, JavaScript)를 통해 질문을 입력합니다.
2.  **API 요청**: 프론트엔드는 백엔드의 `HotelApiController`로 질문을 담아 SSE(Server-Sent Events) 연결을 요청합니다.
3.  **관련 정보 검색**: 컨트롤러는 질문과 가장 관련성 높은 호텔 정보를 `VectorStore`(인-메모리)에서 검색합니다. 이 과정에서 `EmbeddingModel`이 문장 임베딩을 위해 사용됩니다.
4.  **프롬프트 생성 및 LLM 요청**: 검색된 정보를 컨텍스트로 삼아, 사용자의 원본 질문과 함께 프롬프트를 구성하고 `ChatClient`를 통해 외부 LLM(예: OpenAI)에게 답변을 요청합니다.
5.  **답변 스트리밍 및 표시**: LLM이 생성하는 답변은 SSE 스트림을 통해 실시간으로 프론트엔드에 전달되며, 사용자는 타이핑되는 것처럼 보이는 답변을 즉시 확인할 수 있습니다.

## ✨ 주요 기능

- **AI 챗봇**: 호텔 정보에 특화된 AI 챗봇 기능
- **RAG(검색 증강 생성)**: VectorDB에 저장된 최신 호텔 정보를 기반으로 정확하고 일관성 있는 답변 제공
- **실시간 스트리밍 응답**: SSE(Server-Sent Events) 기술을 활용하여 LLM의 답변을 실시간으로 스트리밍
- **임베딩 캐싱**: 한번 임베딩된 문서는 캐시에 저장하여 중복 연산을 최소화하고 응답 속도 향상
- **간편한 실행 환경**: 외부 데이터베이스 없이 인-메모리 VectorStore를 사용하여 어디서든 간편하게 실행 가능

## 🛠️ 사용된 기술

- **Backend**: Kotlin, Spring Boot 3.3.4, Spring AI 1.0.0-M6
- **AI**:
  - `spring-ai-openai-spring-boot-starter` (LLM 연동)
  - `spring-ai-transformers-spring-boot-starter` (On-premise 임베딩 모델)
  - In-Memory `SimpleVectorStore`
- **Frontend**: Thymeleaf, HTML, Bootstrap 5, JavaScript
- **Build**: Gradle
- **Runtime**: Java 21

## 🚀 실행 방법

### 1. 사전 준비

- Java (JDK) 21 이상
- (선택) OpenAI 등 LLM 서비스의 API 키

### 2. 환경 설정

1.  `src/main/resources/` 경로에 `application.properties` 또는 `application.yml` 파일을 생성합니다.
2.  아래와 같이 LLM API 키를 추가합니다. (OpenAI 예시)
    ```properties
    spring.ai.openai.api-key=<YOUR_OPENAI_API_KEY>
    ```

### 3. 애플리케이션 실행

프로젝트 루트 디렉토리에서 터미널을 열고 아래 명령어를 실행합니다.

```bash
# Linux / macOS
./gradlew bootRun

# Windows
.\gradlew.bat bootRun
```

### 4. 접속

애플리케이션 실행 후, 웹 브라우저에서 `http://localhost:8080`으로 접속합니다.

## 📂 프로젝트 구조

```
ko_hotel
├── build.gradle.kts                # 프로젝트 의존성 및 빌드 설정
└── src
    └── main
        ├── kotlin/com/sleekydz86
        │   ├── KoHotelApplication.kt     # Spring Boot 메인 애플리케이션
        │   ├── domain/hotel              # 호텔 도메인 비즈니스 로직
        │   │   ├── controller
        │   │   │   ├── HotelApiController.kt # 챗봇 API 컨트롤러
        │   │   │   └── HotelController.kt    # 웹 페이지 서빙 컨트롤러
        │   │   └── service
        │   │       └── HotelService.kt     # 호텔 정보 및 벡터 검색 서비스
        │   └── global/config             # 전역 설정
        │       ├── CachedEmbeddingModel.kt # 임베딩 캐시 구현체
        │       ├── EmbeddingConfig.kt    # EmbeddingModel 빈 설정
        │       └── VectorStoreConfig.kt  # VectorStore 빈 설정 및 데이터 초기화
        └── resources
            └── templates
                └── hotel.html            # 메인 화면 UI 템플릿
```
