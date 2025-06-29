# WebRTC 1:1 화상통화 애플리케이션

![image](https://github.com/user-attachments/assets/a904815f-d5cb-4619-a3b7-a45086c2b190)

<br/>

Spring WebFlux와 Thymeleaf를 기반으로 한 실시간 1:1 화상통화 애플리케이션입니다.

## 🚀 주요 기능

- **실시간 1:1 화상통화**: WebRTC 기술을 사용한 P2P 연결
- **반응형 웹 인터페이스**: 모든 디바이스에서 사용 가능
- **실시간 통신**: WebSocket을 통한 시그널링
- **미디어 제어**: 비디오/오디오 온오프 기능
- **자동 연결**: 사용자 입장 시 자동 매칭

## 🛠 기술 스택

- **Backend**: Spring Boot 3.2, Spring WebFlux
- **Frontend**: Thymeleaf, Vanilla JavaScript
- **실시간 통신**: WebSocket, WebRTC
- **Build Tool**: Maven
- **Container**: Docker

## 📋 사전 요구사항

- Java 17 이상
- Maven 3.6 이상
- 모던 웹 브라우저 (Chrome, Firefox, Safari, Edge)
- HTTPS 환경 (프로덕션 환경에서 WebRTC 사용 시 필수)

## 🔧 설치 및 실행

### 1. 로컬 개발 환경

```bash
# 프로젝트 클론
git clone <repository-url>
cd webrtc-videocall

# 의존성 설치 및 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run
```

### 2. Docker 사용

```bash
# Docker 이미지 빌드
docker build -t webrtc-videocall .

# 컨테이너 실행
docker run -p 8080:8080 webrtc-videocall
```

### 3. Docker Compose 사용

```bash
# 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f
```

## 🌐 사용 방법

1. **애플리케이션 접속**: `http://localhost:8080`
2. **사용자 이름 입력**: 선택사항 (자동 생성됨)
3. **통화방 입장**: "통화방 입장" 버튼 클릭
4. **상대방 대기**: 다른 사용자가 입장할 때까지 대기
5. **화상통화 시작**: 자동으로 연결 시작

### 제어 버튼

- **📹 비디오**: 카메라 온/오프
- **🎤 오디오**: 마이크 음소거/해제
- **📞 종료**: 통화 종료 및 메인 페이지로 이동

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/example/videocall/
│   │   ├── VideoCallApplication.java        # 메인 애플리케이션
│   │   ├── VideoCallController.java         # 웹 컨트롤러
│   │   └── SignalingWebSocketHandler.java   # WebSocket 핸들러
│   ├── resources/
│   │   ├── templates/
│   │   │   ├── index.html                  # 메인 페이지
│   │   │   └── room.html                   # 화상통화 페이지
│   │   └── application.properties          # 설정 파일
│   └── static/                             # 정적 리소스
├── Dockerfile                              # Docker 설정
├── docker-compose.yml                      # Docker Compose 설정
├── nginx.conf                             # Nginx 설정
└── pom.xml                                # Maven 설정
```

## 🔐 보안 고려사항

### HTTPS 필수
- WebRTC는 HTTPS 환경에서만 정상 작동
- 프로덕션 환경에서는 SSL 인증서 필요
- Let's Encrypt 등으로 무료 SSL 인증서 발급 가능

### 방화벽 설정
- STUN/TURN 서버 포트 개방 필요
- ICE 후보 수집을 위한 포트 범위 설정

## 🚀 프로덕션 배포

### 1. SSL 인증서 준비
```bash
# Let's Encrypt 인증서 발급 예시
certbot certonly --standalone -d yourdomain.com
```

### 2. 환경 변수 설정
```bash
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8080
export SSL_CERT_PATH=/path/to/cert.pem
export SSL_KEY_PATH=/path/to/key.pem
```

### 3. Docker Compose로 배포
```bash
# 프로덕션 환경으로 시작
docker-compose -f docker-compose.yml up -d
```

## 🔧 커스터마이징

### STUN/TURN 서버 변경
`room.html`의 `iceServers` 설정을 수정:

```javascript
const iceServers = {
    iceServers: [
        { urls: 'stun:your-stun-server.com:3478' },
        { 
            urls: 'turn:your-turn-server.com:3478',
            username: 'your-username',
            credential: 'your-password'
        }
    ]
};
```

### UI 스타일 수정
- `index.html`, `room.html`의 CSS 섹션 수정
- 색상, 레이아웃, 애니메이션 등 커스터마이징 가능

## 🐛 문제 해결

### 카메라/마이크 접근 권한
- 브라우저에서 미디어 접근 권한 허용 필요
- HTTPS 환경에서만 접근 가능

### 연결 실패
- 방화벽 설정 확인
- STUN/TURN 서버 연결 확인
- 네트워크 환경 확인 (NAT, 방화벽)

### WebSocket 연결 오류
- 프록시 설정 확인 (Nginx 등)
- WebSocket 업그레이드 헤더 확인

## 📊 성능 최적화

### 비디오 품질 설정
```javascript
// 고화질 설정
const constraints = {
    video: { 
        width: { ideal: 1920 }, 
        height: { ideal: 1080 },
        frameRate: { ideal: 30 }
    },
    audio: {
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true
    }
};
```

### 대역폭 제한
```javascript
// PeerConnection 설정에 대역폭 제한 추가
const sender = peerConnection.getSenders().find(s => 
    s.track && s.track.kind === 'video'
);
const params = sender.getParameters();
params.encodings[0].maxBitrate = 1000000; // 1Mbps
sender.setParameters(params);
```

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.


## 📞 지원

문제가 발생하거나 질문이 있으시면 이슈를 생성해 주세요.

---

**Happy Video Calling! 🎥✨**
