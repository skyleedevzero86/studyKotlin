# Kafka 장애 테스트 가이드

Ko_Chat Kafka 후처리 파이프라인의 운영 안정성을 검증하기 위한 장애 주입 시나리오입니다.

## 사전 준비

- Kafka, MySQL, Redis, Spring Boot 백엔드 기동
- `app.kafka.enabled=true`
- 관리자 계정으로 로그인
- 관리자 · 메시징 운영 화면 (`/admin/messaging`) 또는 API 사용

```http
GET /api/v1/admin/messaging/operations
Authorization: Bearer {admin-token}
```

---

## 1. Outbox 패턴 검증

### 목적
DB 커밋과 Kafka 발행이 분리되어 있고, 브로커 장애 시에도 이벤트가 유실되지 않는지 확인합니다.

### 절차

1. Kafka 컨테이너 중지
   ```bash
   docker stop rag-kafka
   ```
2. 채팅 메시지 3~5건 전송
3. MySQL 확인
   ```sql
   SELECT status, COUNT(*) FROM outbox_events GROUP BY status;
   ```
   - `PENDING` 건수가 증가해야 합니다.
4. 실시간 채팅(WebSocket/Redis)은 정상 동작해야 합니다.
5. Kafka 재기동
   ```bash
   docker start rag-kafka
   ```
6. 10~30초 후 `PENDING` → `PUBLISHED` 전환 확인

### 기대 결과
- 메시지 DB 저장 성공
- Outbox에 이벤트 잔존
- Relay 후 Kafka 발행 완료

---

## 2. Retry / DLQ 검증

### 목적
Consumer 처리 실패 시 재시도 후 DLQ로 이동하는지 확인합니다.

### 절차 (Milvus 장애 시나리오)

1. Milvus 중지 또는 `app.milvus.enabled=false` 후 백엔드 재시작
2. 이미지/파일 첨부 메시지 전송
3. Kafka 토픽 확인
   - `chat.attachment.events.retry-*` 재시도 토픽
   - `chat.attachment.events.dlq` DLQ 토픽
4. DB 확인
   ```sql
   SELECT * FROM dlq_events ORDER BY received_at DESC LIMIT 10;
   ```
5. 관리자 API로 DLQ 재발행
   ```http
   POST /api/v1/admin/messaging/dlq/{dlqEventId}/replay
   ```

### 기대 결과
- 재시도 후 DLQ 보관
- `dlq_events` 테이블에 기록
- Milvus 복구 후 replay 성공

---

## 3. processed_events 멱등성 검증

### 목적
동일 이벤트가 중복 소비되어도 감사 로그/검색 인덱스가 중복 생성되지 않는지 확인합니다.

### 절차

1. DLQ에서 동일 payload를 원본 토픽으로 2회 replay
2. DB 확인
   ```sql
   SELECT event_id, consumer_name, COUNT(*)
   FROM processed_events
   GROUP BY event_id, consumer_name
   HAVING COUNT(*) > 1;
   ```
3. 감사 로그 중복 확인
   ```sql
   SELECT event_id, COUNT(*) FROM chat_audit_logs GROUP BY event_id HAVING COUNT(*) > 1;
   ```

### 기대 결과
- `processed_events` unique 제약으로 중복 처리 차단
- audit/search 테이블에 동일 event_id 중복 없음

---

## 4. Kafka Lag 모니터링 검증

### 목적
Consumer 지연(lag)이 감지되고 health 상태에 반영되는지 확인합니다.

### 절차

1. Kafka consumer 전부 중지 (백엔드만 유지)
2. 메시지 100건 이상 전송
3. 상태 확인
   ```http
   GET /actuator/health
   GET /api/v1/admin/messaging/operations
   ```
4. `consumerLag` 배열에서 `lag` 값 증가 확인
5. `app.kafka.lag-critical-threshold` 초과 시 health `DOWN`

### 기대 결과
- `maxLag` 증가
- warning 로그 출력
- critical 초과 시 `/actuator/health` kafka 관련 상태 악화

---

## 5. Outbox FAILED 복구 검증

### 목적
Relay 최대 재시도 초과 후 FAILED 상태 이벤트를 운영자가 재큐잉할 수 있는지 확인합니다.

### 절차

1. Kafka를 장시간 중지한 채 `outbox-max-retries` 이상 Relay 주기 경과
2. `outbox_events.status = 'FAILED'` 확인
3. Kafka 복구 후 관리자 API 호출
   ```http
   POST /api/v1/admin/messaging/outbox/requeue-failed?limit=50
   ```
4. `PENDING` → `PUBLISHED` 전환 확인

### 기대 결과
- FAILED 이벤트가 PENDING으로 복귀
- Relay가 정상 발행

---

## 체크리스트

| 항목 | 확인 |
| ---- | ---- |
| Kafka 중단 시 채팅 실시간 전달 유지 | ☐ |
| Outbox PENDING 적재 | ☐ |
| Kafka 복구 후 Outbox 발행 | ☐ |
| Consumer retry 토픽 생성 | ☐ |
| DLQ 보관 및 dlq_events 기록 | ☐ |
| processed_events 멱등성 | ☐ |
| Lag 모니터링 API/health | ☐ |
| FAILED outbox 재큐잉 | ☐ |

---

## 관련 설정

| 키 | 기본값 | 설명 |
| --- | --- | --- |
| `app.kafka.outbox-max-retries` | 10 | Outbox relay 최대 재시도 |
| `app.kafka.consumer-retry-attempts` | 4 | Consumer 재시도 횟수 |
| `app.kafka.lag-warning-threshold` | 100 | lag warning |
| `app.kafka.lag-critical-threshold` | 1000 | health DOWN 기준 |
