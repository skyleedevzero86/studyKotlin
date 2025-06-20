![img.png](img.png)

# KoImg

![KoImg Logo](./koimg-logo.png)

**KoImg**는 다양한 이미지 분석 기능을 제공하는 Kotlin 기반 Spring Boot 프로젝트입니다. AI 기반 이미지 분석, 이미지 신뢰도 평가, 이미지 생성 등 여러 기능을 REST API 및 웹 인터페이스로 제공합니다.

---

## 🔑 주요 기능

### AI 이미지 분석

* `/aistudio` 패키지에서 AI 기반 이미지 분석 기능 제공
* 다양한 분석 타입 지원 (`AnalysisType`)
* 분석 요청/응답 모델: `AnalysisRequest`, `AnalysisResult`
* REST API 컨트롤러: `AnalysisImageController`, `ApiAnalyzeController`
* 서비스 레이어: `ImageAnalysisService`

### 예제 기능

* `/example1`, `/example2` 패키지에서 이미지 분석 및 메시지 처리 예제 제공
* 이미지 분석용 컨트롤러 및 DTO 포함
* Groq 기반 메시지 처리 예시 포함

### 글로벌 설정

* `/global/config`에서 프로젝트 전역 설정 관리
* AI 서비스 연동을 위한 설정 예: `ChatConfig`

### 템플릿 기반 웹 UI

* `/resources/templates`에서 이미지 분석, 신뢰도 평가, 이미지 생성 등 다양한 HTML 템플릿 제공

---

## 🧱 프로젝트 구조

```
src/
 └─ main/
     ├─ kotlin/
     │   └─ com/
     │       └─ koimg/
     │           ├─ aistudio/       # AI 이미지 분석 관련 코드
     │           ├─ example1/       # 예제1: 이미지 분석 및 메시지 처리
     │           ├─ example2/       # 예제2: 이미지 분석 예시
     │           └─ global/         # 글로벌 설정
     └─ resources/
         ├─ templates/              # 웹 UI 템플릿 (analyze, imgConfidence, imgWriter 등)
         └─ application-select.yml  # 환경설정 파일 (OpenAI API Key 등)
```

---

## ⚙️ 환경설정

* 설정 파일: `src/main/resources/application-select.yml`
* OpenAI API Key 등 AI 서비스 연동을 위한 설정 포함

---

## 🚀 실행 방법

### 1. 의존성 설치 및 애플리케이션 실행

```bash
./gradlew bootRun
```

### 2. 빌드 및 실행

```bash
./gradlew build
```

### 3. 웹 접속

* 기본 주소: [http://localhost:8080](http://localhost:8080)

---

## 📦 주요 의존성

* Kotlin
* Spring Boot
* (AI 연동 시) OpenAI API

---

## 🤝 기여 방법

* 이슈 등록 및 토론
* PR(Pull Request) 제출

---

## 📄 라이선스

* (필요시 라이선스 명시)
