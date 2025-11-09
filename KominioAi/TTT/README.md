# 리워드 시스템 (Reward System)

프론트엔드의 리워드 시스템을 참조하여 구현한 Kotlin 백엔드 도메인입니다.

## 📁 프로젝트 구조

```
TTT/src/main/kotlin/com/kominioai/domain/reward/
├── domain/
│   ├── model/           # 도메인 모델
│   ├── repository/      # 리포지토리 인터페이스
│   ├── service/         # 도메인 서비스
│   └── event/           # 도메인 이벤트
├── application/
│   ├── port/
│   │   ├── in/          # 인바운드 포트 (Use Case)
│   │   └── out/         # 아웃바운드 포트 (Persistence, Event)
│   ├── service/         # 애플리케이션 서비스
│   └── dto/             # 데이터 전송 객체
└── adapter/
    ├── in/web/          # 웹 컨트롤러
    └── out/
        ├── persistence/ # 데이터베이스 어댑터
        └── event/       # 이벤트 퍼블리셔 어댑터
```

## 🎯 주요 기능

### 1. 참여자 리워드 (Participant Reward)

- 설문/퀴즈 참여 시 리워드 지급
- 확률 기반 리워드 추첨
- 클레임 코드 생성 및 관리
- 리워드 수령 및 만료 처리

### 2. 생성자 리워드 (Creator Reward)

- 설문 참여율 달성 시 리워드 지급
- 참여율에 따른 차등 리워드 계산
- 생성자 리워드 수령 관리

### 3. 리워드 풀 관리 (Reward Pool)

- 전역 리워드 풀 관리
- 예산 관리 및 사용량 추적
- 리워드 풀 활성화/비활성화

## 🏗️ 도메인 모델

### 핵심 엔티티

- **Reward**: 리워드 정보
- **UserReward**: 사용자별 리워드 지급 내역
- **CreatorReward**: 생성자 리워드
- **RewardPool**: 리워드 풀

### 값 객체

- **RewardId**: 리워드 식별자
- **ClaimCode**: 클레임 코드
- **RewardType**: 리워드 타입 (기프티콘, 포인트, 쿠폰 등)
- **RewardStatus**: 리워드 상태 (대기중, 수령완료, 만료됨)

## 🔄 비즈니스 로직

### 참여자 리워드 처리

1. 설문/퀴즈 참여 시 리워드 설정 확인
2. 확률 기반 리워드 당첨 여부 결정
3. 당첨 시 UserReward 생성 및 클레임 코드 발급
4. RewardWonEvent 발행

### 생성자 리워드 처리

1. 설문 참여율 계산
2. 목표 참여율 달성 여부 확인
3. 달성 시 CreatorReward 생성
4. CreatorRewardEarnedEvent 발행

## 📊 API 엔드포인트

### 리워드 관리

- `POST /api/rewards` - 리워드 생성
- `PUT /api/rewards/{id}` - 리워드 수정
- `DELETE /api/rewards/{id}` - 리워드 삭제
- `GET /api/rewards/{id}` - 리워드 조회
- `GET /api/rewards` - 리워드 목록 조회

### 참여자 리워드

- `POST /api/rewards/participant` - 참여자 리워드 처리
- `POST /api/rewards/claim` - 리워드 수령
- `GET /api/rewards/user/{userId}` - 사용자 리워드 조회

### 생성자 리워드

- `POST /api/creator-rewards/calculate` - 생성자 리워드 계산
- `POST /api/creator-rewards/claim` - 생성자 리워드 수령
- `GET /api/creator-rewards/creator/{creatorId}` - 생성자 리워드 조회
- `GET /api/creator-rewards/creator/{creatorId}/statistics` - 생성자 리워드 통계

### 통계

- `GET /api/rewards/statistics` - 리워드 통계

## 🎨 프론트엔드 연동

이 백엔드 시스템은 프론트엔드의 다음 기능들과 연동됩니다:

1. **참여자 리워드 설정**: 설문/퀴즈 생성 시 리워드 설정
2. **리워드 당첨 모달**: 참여 완료 시 리워드 당첨 표시
3. **클레임 코드**: 리워드 수령을 위한 고유 코드
4. **리워드 히스토리**: 사용자별 리워드 지급 내역
5. **생성자 리워드**: 설문 참여율 달성 시 리워드 지급
6. **리워드 통계**: 관리자용 리워드 현황 대시보드

## 🔧 기술 스택

- **Kotlin**: 메인 프로그래밍 언어
- **Spring WebFlux**: 리액티브 웹 프레임워크
- **R2DBC**: 리액티브 데이터베이스 접근
- **Spring Data R2DBC**: 리액티브 리포지토리
- **Spring Events**: 도메인 이벤트 발행
- **Bean Validation**: 요청 데이터 검증

## 📝 사용 예시

### 참여자 리워드 처리

```kotlin
val request = ProcessParticipantRewardRequest(
    userId = "user123",
    surveyId = "survey456",
    type = RewardType.GIFTCARD,
    value = 10000,
    description = "기프티콘 10,000원",
    probability = 0.1 // 10% 확률
)

rewardUseCase.processParticipantReward(request)
    .subscribe { response ->
        if (response.won) {
            // 리워드 당첨 처리
            showRewardModal(response)
        }
    }
```

### 생성자 리워드 계산

```kotlin
val request = CalculateCreatorRewardRequest(
    surveyId = "survey456",
    creatorId = "creator789",
    participationCount = 80,
    targetCount = 100,
    baseRewardValue = 5000
)

creatorRewardUseCase.calculateCreatorReward(request)
    .subscribe { response ->
        // 생성자 리워드 지급 처리
    }
```

## 🚀 향후 개선 사항

1. **리워드 풀 관리**: 전역 리워드 풀 관리 기능 추가
2. **리워드 만료 처리**: 자동 만료 처리 스케줄러
3. **리워드 알림**: 이메일/SMS 알림 기능
4. **리워드 분석**: 상세한 리워드 통계 및 분석
5. **리워드 정책**: 복잡한 리워드 정책 관리
