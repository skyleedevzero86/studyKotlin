# Text Analysis Application<br/>

![image](https://github.com/user-attachments/assets/aa1a6d9a-db5e-4c42-94e5-9810e2ebce47)

RAG Study - 텍스트 분석 애플리케이션
Kotlin Spring Boot 기반의 RAG(Retrieval-Augmented Generation) 기술을 활용한 다양한 자연어 처리 기능을 제공하는 텍스트 분석 애플리케이션입니다.

## Project Overview

이 애플리케이션은 최신 자연어 처리 기술을 기반으로 다양한 텍스트 분석 기능을 구현하고 있습니다. Kotlin과 Spring Boot를 기반으로 구축되었으며, 실제로 활용 가능한 RAG 기법을 중심으로 개발되었습니다.

## Features

- **텍스트 유사도 분석:**: 두 개의 텍스트 문서 간 유사도를 비교합니다.
- **문서 클러스터링**: 유사한 문서들을 클러스터링 알고리즘을 통해 그룹화합니다.
- **키워드 추출**: 텍스트에서 중요한 키워드를 추출합니다.
- **감정 분석**: 텍스트의 감정(긍정/부정/중립)을 분석합니다.
- **텍스트 요약**: 긴 텍스트를 간결한 요약으로 생성합니다.
- **주제 분류**: 텍스트의 주제나 카테고리를 식별합니다.

## Technology Stack

- 언어: Kotlin
- 프레임워크: Spring Boot 3.x
- AI/ML: Spring AI (v1.0.0-M8)
- 텍스트 처리: Apache Lucene
- 머신러닝: Smile Core 라이브러리
- 캐싱: Spring Boot Cache + Redis
- API 문서화: SpringDoc OpenAPI UI
- 벡터 기능: Transformers (ONNX) 임베딩

## 시작하기

### 사전 준비 사항
- JDK 19 이상
- Gradle
- Redis (선택 사항, 캐시 용도)


### Build and Run

1. 프로젝트 설정 적용
2. 애플리케이션 실행

애플리케이션 실행 후 `http://localhost:8080` 에서 확인 가능합니다.

## API 엔드포인트

| Endpoint                                      | Method | Description                              |
|----------------------------------------------|--------|------------------------------------------|
| `/api/v1/text-analysis/similarity`           | GET    | 두 텍스트 간 유사도 계산   |
| `/api/v1/text-analysis/keywords`             | GET    | 텍스트에서 키워드 추출               |
| `/api/v1/text-analysis/sentiment`            | GET    | 텍스트의 감정 분석                |
| `/api/v1/text-analysis/cluster`              | POST   | 문서 집합 클러스터링               |
| `/api/v1/text-analysis/summarize`            | GET    | 텍스트 요약 생성               |
| `/api/v1/text-analysis/classify-topic`       | GET    | 텍스트 주제 분류                   |
| `/api/v1/text-analysis/tokenize`             | GET    | 텍스트 토큰화          |

## 프로젝트 구조

- `com.ragstudy`: 메인 패키지
- `domain`: 핵심 비즈니스 로직
- `controller`: API 컨트롤러
- `service`: 서비스 구현체
- `model`: 도메인 모델
- `dto`: 데이터 전송 객체
- `global`:공통 처리 로직

## 라이선스 [라이선스 명시]

## 감사의 말

- Spring AI 사용
- Apache Lucene 기반 텍스트 분석
- Smile 머신러닝 라이브러리
- Spring Boot 프레임워크 활용
