![img.png](img.png)

# 설문조사 시스템 (Survey Management System)

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green.svg)](https://spring.io/projects/spring-boot)
[![Spring WebFlux](https://img.shields.io/badge/Spring%20WebFlux-Reactive-orange.svg)](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
[![Architecture](https://img.shields.io/badge/Architecture-DDD%20%2B%20Hexagonal-purple.svg)](https://en.wikipedia.org/wiki/Domain-driven_design)

## 📋 프로젝트 개요

본 프로젝트는 **DDD(Domain-Driven Design)**와 **헥사고날 아키텍처**를 기반으로 설계된 현대적인 설문조사 관리 시스템입니다. 대규모 동시 응답 처리와 실시간 통계 제공을 목표로 **Spring WebFlux**를 활용한 반응형 프로그래밍을 적용했습니다.

### 🎯 주요 특징

- **도메인 중심 설계**: 비즈니스 로직과 기술 구현의 완전한 분리
- **반응형 아키텍처**: 대용량 트래픽 처리를 위한 비동기/논블로킹 처리
- **확장 가능한 구조**: 마이크로서비스 전환 준비된 모듈형 설계
- **실시간 통계**: WebFlux 스트리밍을 활용한 라이브 데이터 제공
- **타입 안정성**: Kotlin의 강력한 타입 시스템과 함수형 프로그래밍 활용

## 🏗️ 시스템 아키텍처

### 헥사고날 아키텍처 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    Primary Adapters                         │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐│
│  │   Web API       │ │   Scheduled     │ │   Event         ││
│  │   Controllers   │ │   Tasks         │ │   Handlers      ││
│  └─────────────────┘ └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────────────────────────┘
                               │
┌─────────────────────────────────────────────────────────────┐
│                Application Layer (Ports)                    │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              Primary Ports (Use Cases)                 ││
│  │   CreateSurvey │ SubmitResponse │ GenerateStatistics   ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │            Application Services                         ││
│  │   SurveyService │ ResponseService │ StatisticsService   ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │            Secondary Ports                              ││
│  │   Repositories │ NotificationPort │ ExternalDataPort    ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                               │
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                             │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐│
│  │   Survey    │ │  Question   │ │  Response   │ │Participant││
│  │  Aggregate  │ │  Aggregate  │ │  Aggregate  │ │Aggregate││
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │              Domain Services                            ││
│  │   StatisticsCalculator │ ValidationService              ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │              Shared Kernel                              ││
│  │   DomainEvent │ AggregateRoot │ ValueObject             ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                               │
┌─────────────────────────────────────────────────────────────┐
│                Secondary Adapters                           │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐│
│  │   Database      │ │   External      │ │   Notification  ││
│  │   (JPA/R2DBC)   │ │   APIs          │ │   Services      ││
│  └─────────────────┘ └─────────────────┘ └─────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### 도메인 모델 관계도

```
Survey (설문)
├── SurveyId (설문 ID)
├── SurveyTitle (제목)
├── SurveyPeriod (기간)
├── SurveyStatus (상태)
├── TargetGroup (대상 그룹)
└── Questions (질문들) ──┐
                        │
Question (질문) ──────────┘
├── QuestionId (질문 ID)
├── QuestionText (질문 내용)
├── QuestionType (질문 유형)
├── QuestionOrder (순서)
└── Choices (선택지들) ──┐
                       │
Choice (선택지) ────────┘
├── ChoiceId (선택지 ID)
├── ChoiceText (선택지 텍스트)
└── ChoiceOrder (순서)

Response (응답)
├── ResponseId (응답 ID)
├── SurveyId (설문 ID)
├── ParticipantId (참여자 ID)
├── ResponseStatus (응답 상태)
└── Answers (답변들) ──┐
                      │
Answer (답변) ─────────┘
├── AnswerId (답변 ID)
├── QuestionId (질문 ID)
├── AnswerValue (답변 값)
└── CreatedAt (생성일시)

Participant (참여자)
├── ParticipantId (참여자 ID)
├── ContactInfo (연락처 정보)
├── Demographics (인구통계)
│   ├── AgeGroup (연령대)
│   ├── Gender (성별)
│   └── Region (지역)
└── CreatedAt (생성일시)
```

## 📊 데이터베이스 설계

### ERD 핵심 관계

```
┌─────────────┐    1:N    ┌─────────────┐    1:N    ┌─────────────┐
│   SURVEY    │ ────────→ │  QUESTION   │ ────────→ │   CHOICE    │
│             │           │             │           │             │
│ - id (PK)   │           │ - id (PK)   │           │ - id (PK)   │
│ - title     │           │ - survey_id │           │ - question_id│
│ - status    │           │ - text      │           │ - text      │
│ - period    │           │ - type      │           │ - order     │
└─────────────┘           └─────────────┘           └─────────────┘
        │                         │
        │                         │
        │ 1:N                     │ 1:N
        ▼                         ▼
┌─────────────┐    1:N    ┌─────────────┐
│  RESPONSE   │ ────────→ │   ANSWER    │
│             │           │             │
│ - id (PK)   │           │ - id (PK)   │
│ - survey_id │           │ - response_id│
│ - participant_id        │ - question_id│
│ - status    │           │ - value     │
└─────────────┘           └─────────────┘
        │
        │ N:1
        ▼
┌─────────────┐
│ PARTICIPANT │
│             │
│ - id (PK)   │
│ - name      │
│ - phone     │
│ - demographics│
└─────────────┘
```

## 📁 프로젝트 구조

```
src/main/kotlin/com/survey/
├── application/                    # 애플리케이션 계층
│   ├── port/
│   │   ├── input/                 # 입력 포트 (Use Cases)
│   │   │   ├── survey/
│   │   │   ├── response/
│   │   │   └── statistics/
│   │   └── output/                # 출력 포트
│   │       ├── persistence/
│   │       ├── notification/
│   │       └── external/
│   └── service/                   # 애플리케이션 서비스
│       ├── SurveyApplicationService.kt
│       ├── ResponseApplicationService.kt
│       └── StatisticsApplicationService.kt
│
├── domain/                        # 도메인 계층
│   ├── survey/                    # Survey Aggregate
│   │   ├── Survey.kt             # Aggregate Root
│   │   ├── SurveyId.kt           # Value Object
│   │   ├── SurveyTitle.kt        # Value Object
│   │   └── service/
│   ├── question/                  # Question Aggregate
│   ├── response/                  # Response Aggregate
│   ├── participant/               # Participant Aggregate
│   ├── statistics/                # Statistics Domain Service
│   └── shared/                    # Shared Kernel
│
├── infrastructure/                # 인프라스트럭처 계층
│   ├── adapter/
│   │   ├── input/                # Primary Adapters
│   │   │   └── web/
│   │   └── output/               # Secondary Adapters
│   │       ├── persistence/
│   │       └── external/
│   └── config/
│
└── SurveyApplication.kt          # Main Application Class
```

## 🚀 핵심 기능

### 1. 설문조사 관리
- **설문 생성/수정/삭제**: 다양한 질문 유형 지원
- **대상 그룹 설정**: 연령대, 성별, 지역별 타겟팅
- **설문 상태 관리**: 임시저장 → 활성화 → 완료 → 취소

### 2. 응답 수집
- **실시간 응답 처리**: WebFlux를 활용한 고성능 처리
- **조건부 로직**: 이전 답변에 따른 동적 질문 표시
- **진행률 추적**: 실시간 응답 완료율 계산

### 3. 통계 및 분석
- **실시간 통계**: 라이브 응답률, 참여자 분포
- **인구통계 분석**: 연령대, 성별, 지역별 분석
- **응답 패턴 분석**: 질문별 선택 분포, 텍스트 분석

### 4. 알림 및 내보내기
- **자동 알림**: 설문 완료, 마감 임박 알림
- **데이터 내보내기**: Excel, CSV 형태로 결과 다운로드

## 🛠️ 기술 스택

### Backend
- **Language**: Kotlin 1.9+
- **Framework**: Spring Boot 3.0+, Spring WebFlux
- **Database**: PostgreSQL + R2DBC (반응형 DB 접근)
- **ORM**: Spring Data R2DBC
- **Build Tool**: Gradle Kotlin DSL

### Architecture Patterns
- **Domain-Driven Design (DDD)**
- **Hexagonal Architecture (Ports & Adapters)**
- **CQRS (Command Query Responsibility Segregation)** - 준비됨
- **Event-Driven Architecture**

### Development
- **Testing**: JUnit 5, MockK, Testcontainers
- **Documentation**: Spring REST Docs
- **Code Quality**: Detekt, KtLint

## 🔧 설치 및 실행

### 사전 요구사항
- JDK 17+
- Docker & Docker Compose
- PostgreSQL 14+

### 실행 방법

1. **저장소 클론**
```bash
git clone https://github.com/your-org/survey-system.git
cd survey-system
```

2. **데이터베이스 설정**
```bash
docker-compose up -d postgres
```

3. **애플리케이션 실행**
```bash
./gradlew bootRun
```

4. **API 문서 확인**
```
http://localhost:8080/docs/api-guide.html
```

## 📡 API 엔드포인트

### 설문조사 관리
```http
POST   /api/v1/surveys              # 설문 생성
GET    /api/v1/surveys              # 설문 목록 조회
GET    /api/v1/surveys/{id}         # 설문 상세 조회
PUT    /api/v1/surveys/{id}         # 설문 수정
DELETE /api/v1/surveys/{id}         # 설문 삭제
POST   /api/v1/surveys/{id}/start   # 설문 시작
```

### 응답 관리
```http
POST   /api/v1/surveys/{id}/responses     # 응답 시작
PUT    /api/v1/responses/{id}/answers     # 답변 저장
POST   /api/v1/responses/{id}/submit      # 응답 제출
GET    /api/v1/responses/{id}             # 응답 조회
```

### 통계 및 분석
```http
GET    /api/v1/surveys/{id}/statistics           # 설문 통계 조회
GET    /api/v1/surveys/{id}/statistics/stream    # 실시간 통계 스트림
GET    /api/v1/surveys/{id}/export               # 결과 내보내기
```

### ERD

```mermaid
erDiagram
    %% 설문조사 시스템 ERD
    
    SURVEY {
        bigint id PK "설문 ID"
        varchar title "설문 제목"
        text description "설문 설명"
        date start_date "시작일"
        date end_date "종료일"
        varchar status "상태(DRAFT,ACTIVE,COMPLETED,CANCELLED)"
        varchar target_age_groups "대상 연령대(JSON)"
        varchar target_genders "대상 성별(JSON)"
        varchar target_regions "대상 지역(JSON)"
        timestamp created_at "생성일시"
        timestamp updated_at "수정일시"
        bigint created_by "생성자 ID"
    }
    
    QUESTION {
        bigint id PK "질문 ID"
        bigint survey_id FK "설문 ID"
        text question_text "질문 내용"
        varchar question_type "질문 유형(SINGLE_CHOICE,MULTIPLE_CHOICE,TEXT,TEXTAREA)"
        int question_order "질문 순서"
        boolean is_required "필수 여부"
        text conditional_logic "조건부 로직(JSON)"
        timestamp created_at "생성일시"
    }
    
    CHOICE {
        bigint id PK "선택지 ID"
        bigint question_id FK "질문 ID"
        varchar choice_text "선택지 텍스트"
        int choice_order "선택지 순서"
        timestamp created_at "생성일시"
    }
    
    PARTICIPANT {
        bigint id PK "참여자 ID"
        varchar name "이름"
        varchar phone_number "전화번호"
        varchar email "이메일"
        varchar age_group "연령대(TEENS,TWENTIES,THIRTIES,FORTIES,FIFTIES_PLUS)"
        varchar gender "성별(MALE,FEMALE,OTHER)"
        varchar region "지역"
        timestamp created_at "생성일시"
        timestamp updated_at "수정일시"
    }
    
    RESPONSE {
        bigint id PK "응답 ID"
        bigint survey_id FK "설문 ID"
        bigint participant_id FK "참여자 ID"
        varchar status "응답 상태(IN_PROGRESS,COMPLETED,EXPIRED)"
        timestamp started_at "응답 시작일시"
        timestamp submitted_at "제출일시"
        double completion_rate "완료율"
    }
    
    ANSWER {
        bigint id PK "답변 ID"
        bigint response_id FK "응답 ID"
        bigint question_id FK "질문 ID"
        text text_value "텍스트 답변"
        text choice_ids "선택한 선택지 ID들(JSON)"
        double numeric_value "숫자 답변"
        timestamp created_at "생성일시"
    }
    
    SURVEY_STATISTICS {
        bigint id PK "통계 ID"
        bigint survey_id FK "설문 ID"
        int total_participants "총 참여자 수"
        int completed_responses "완료된 응답 수"
        double response_rate "응답률"
        date statistics_date "통계 생성일"
        text demographics_stats "인구통계 통계(JSON)"
        timestamp created_at "생성일시"
    }
    
    QUESTION_STATISTICS {
        bigint id PK "질문별 통계 ID"
        bigint survey_id FK "설문 ID"
        bigint question_id FK "질문 ID"
        text choice_statistics "선택지별 통계(JSON)"
        text text_analysis "텍스트 분석 결과(JSON)"
        timestamp created_at "생성일시"
    }
    
    %% 관계 정의
    SURVEY ||--o{ QUESTION : "has"
    QUESTION ||--o{ CHOICE : "has"
    SURVEY ||--o{ RESPONSE : "receives"
    PARTICIPANT ||--o{ RESPONSE : "submits"
    RESPONSE ||--o{ ANSWER : "contains"
    QUESTION ||--o{ ANSWER : "answered_in"
    SURVEY ||--o{ SURVEY_STATISTICS : "has"
    SURVEY ||--o{ QUESTION_STATISTICS : "has"
    QUESTION ||--o{ QUESTION_STATISTICS : "analyzed_in"


## 🧪 테스트 전략

### 테스트 피라미드
```
         ┌─────────────────┐
         │   E2E Tests     │  ← 통합 시나리오 테스트
         │    (적음)        │
         └─────────────────┘
       ┌───────────────────────┐
       │  Integration Tests    │  ← API, 데이터베이스 테스트
       │       (중간)          │
       └───────────────────────┘
   ┌─────────────────────────────────┐
   │      Unit Tests                 │  ← 도메인 로직, 비즈니스 규칙
   │        (많음)                   │
   └─────────────────────────────────┘
```

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 도메인 테스트만
./gradlew test --tests "*domain*"

# 통합 테스트만
./gradlew test --tests "*integration*"

# 테스트 커버리지 리포트
./gradlew jacocoTestReport
```

## 📈 성능 고려사항

### 확장성
- **수평 확장**: 상태가 없는 애플리케이션 설계
- **데이터베이스 샤딩**: 설문별 데이터 분산 준비
- **캐싱 전략**: Redis를 활용한 통계 데이터 캐싱

### 성능 최적화
- **반응형 스트림**: 백프레셀 기반 메모리 효율적 처리
- **인덱스 최적화**: 쿼리 성능을 위한 복합 인덱스
- **배치 처리**: 대량 통계 계산의 비동기 처리

## 🤝 기여 가이드

### 코드 스타일
- Kotlin 공식 코딩 컨벤션 준수
- 함수형 프로그래밍 스타일 권장
- 도메인 중심의 명확한 네이밍

### 커밋 메시지
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
refactor: 코드 리팩토링
test: 테스트 추가/수정
```

## 📝 라이선스

이 프로젝트는 MIT 라이선스하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.


---

## 🎯 향후 로드맵

- [ ] **마이크로서비스 분리**: 설문 관리 / 응답 처리 / 통계 분석
- [ ] **실시간 협업**: WebSocket 기반 다중 사용자 설문 편집
- [ ] **AI 분석**: 자연어 처리를 통한 주관식 답변 분석
- [ ] **모바일 지원**: React Native 기반 모바일 앱
- [ ] **글로벌화**: i18n 지원 및 다국어 설문

**📞 문의사항이나 제안사항이 있으시면 언제든 이슈를 등록해주세요!**