# Ko Voice

Kotlin + Spring Boot 기반 **음성 메모 / STT·TTS** 웹 애플리케이션입니다.
외부 음성 처리 API를 사용해 음성→텍스트, 텍스트→음성 변환을 제공합니다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| 언어 | Kotlin 1.9.25 |
| 프레임워크 | Spring Boot 3.5.0 |
| JDK | 21 |
| 뷰 | Thymeleaf |
| HTTP 클라이언트 | Apache HttpClient 5 |
| 빌드 | Gradle (Kotlin DSL) |

---

## 프로젝트 구조

```
ko_voice/
├── build.gradle.kts
├── src/main/
│   ├── kotlin/com/voice/
│   │   ├── KoVoiceApplication.kt          # 진입점
│   │   ├── domain/
│   │   │   ├── speech/                    # TTS 모델/음성 설정
│   │   │   │   └── TtsOptions.kt
│   │   │   ├── voicememo/                 # 음성 메모 도메인
│   │   │   │   ├── controller/
│   │   │   │   │   └── VoiceController.kt # /voice, /transcribe, /generate, /audio
│   │   │   │   ├── service/
│   │   │   │   │   ├── VoiceService.kt    # STT/TTS 오케스트레이션
│   │   │   │   │   └── SpeechApiClient.kt # 외부 API 호출
│   │   │   │   └── dto/
│   │   │   │       ├── SpeechRequest.kt
│   │   │   │       ├── TranscriptionRequest.kt
│   │   │   │       ├── TranscriptionResponse.kt
│   │   │   │       └── ...
│   │   │   └── speechapp/
│   │   │       └── controller/
│   │   │           └── SpeechController.kt # /, /speech-to-text, /text-to-speech
│   │   └── global/
│   │       └── config/
│   │           ├── AppConfig.kt           # HttpClient, uploadDir 빈
│   │           └── util/
│   │               └── KoreanRomanizer.kt # 한글 로마자 변환 (TTS 전처리)
│   └── resources/
│       ├── application.yml               # 앱·외부 API·파일 업로드 설정
│       └── templates/
│           ├── voicememo/Voice.html       # 음성 메모 UI
│           └── audioprocessor/speechapp.html
└── uploads/                               # 생성/업로드된 오디오 (커밋 제외)
```

---

## 주요 기능

- **STT (Speech-to-Text)**
  - 음성 파일 업로드 → 외부 STT API로 텍스트 변환
  - 언어: 한국어(ko), 영어(en), 일본어(ja), 중국어(zh) 등 지원

- **TTS (Text-to-Speech)**
  - 텍스트 입력 → 외부 TTS API로 음성 파일 생성
  - 영어/아랍어 모델과 여러 음성 선택 가능
  - 한글 텍스트는 `KoreanRomanizer`로 전처리 후 API 전달

- **파일 저장**
  - 업로드/생성 오디오는 `file.storage.path`(기본 `./uploads`)에 저장
  - Voice 메모: `/audio/{fileName}` 로 재생/다운로드

---

## 설정 (application.yml)

```yaml
spring:
  application:
    name: ko-voice
  servlet:
    multipart:
      max-file-size: 25MB
      max-request-size: 25MB

speech:
  api:
    key: ${SPEECH_API_KEY:your-key}
    base-url: ${SPEECH_API_BASE_URL:https://api.example.com/openai/v1}
    stt-endpoint: /audio/transcriptions
    tts-endpoint: /audio/speech
    tts:
      default-model: english
      default-voice: troy
      models:
        english: ${SPEECH_TTS_ENGLISH_MODEL:your-english-tts-model}
        arabic: ${SPEECH_TTS_ARABIC_MODEL:your-arabic-tts-model}

file:
  storage:
    path: ./uploads
```

- **API 키**: 프로덕션에서는 `SPEECH_API_KEY` 환경 변수로 설정하는 것을 권장합니다.
- **미디어 파일**: `uploads/` 및 주요 오디오/비디오 확장자는 `.gitignore`에 포함되어 있습니다.

---

## 실행 방법

1. **의존성 및 실행**
   ```bash
   ./gradlew bootRun
   ```
   Windows:
   ```powershell
   .\gradlew.bat bootRun
   ```

2. **JAR 빌드 후 실행**
   ```bash
   ./gradlew bootJar
   java -jar build/libs/ko_voice-0.0.1-SNAPSHOT.jar
   ```

3. **접속**
   - 음성 메모 (Thymeleaf): `http://localhost:8080/voice`
   - Speech 앱: `http://localhost:8080/`

---

## API / 엔드포인트 요약

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/` | Speech 앱 메인 |
| GET | `/voice` | 음성 메모 페이지 (STT/TTS 폼) |
| POST | `/transcribe` | 음성 파일 → 텍스트 (폼) |
| POST | `/generate` | 텍스트 → 음성 생성 (폼) |
| GET | `/audio/{fileName}` | 생성된 오디오 스트리밍 |
| POST | `/speech-to-text` | STT (JSON/멀티파트) |
| POST | `/text-to-speech` | TTS (JSON, WAV 응답) |
| GET | `/audio-files` | 업로드된 파일 목록 |
| GET | `/play-audio/{filename}` | 업로드 파일 재생 |

---

## 제한 사항

- 음성 파일: 최대 **25MB**
- TTS 텍스트: 요청당 최대 **200자**
- STT/TTS 모두 외부 API 할당량 및 모델 권한의 영향을 받습니다.

---

## 라이선스

프로젝트 설정에 따릅니다.
