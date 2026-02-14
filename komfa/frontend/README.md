# Frontend (Svelte 5 + DDD + Clean Code + Functional)

- **Runtime**: Svelte 5 (runes), TypeScript
- **Package manager**: pnpm
- **Architecture**: DDD + Clean Code + 함수형

## 구조

```
src/
├── lib/
│   ├── domain/          # 도메인: 타입, 값 객체, 순수 비즈니스 규칙 (부수효과 없음)
│   │   └── counter/
│   ├── application/     # 애플리케이션: 유스케이스, 도메인 조합 (인프라에 의존 주입)
│   │   └── counter/
│   └── infrastructure/  # 인프라: 저장소, API 클라이언트 등 외부 부수효과
│       └── counter/
├── routes/              # SvelteKit 라우트 (프레젠테이션)
└── app.d.ts
```

- **Domain**: 프레임워크/UI 무관, 순수 함수와 불변 타입.
- **Application**: 도메인 + 저장소(포트) 조합. 테스트 시 저장소만 교체 가능.
- **Infrastructure**: 실제 저장소 구현 (메모리, localStorage, API 등).
- **Presentation**: Svelte 5 `$state` 등으로 UI만 담당, 로직은 Application에 위임.

## 명령어 (pnpm)

```bash
pnpm install
pnpm dev        # 개발 서버 (기본 http://localhost:5173)
pnpm build      # 프로덕션 빌드
pnpm preview    # 빌드 결과 미리보기
pnpm check      # 타입/스벨트 검사
```

## Node / pnpm

- Node `^20.19` / `^22.12` / `>=24` 권장. 다른 버전은 `.npmrc`에서 `engine-strict=false`로 설치 가능.
