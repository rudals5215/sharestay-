# ShareStay+ Frontend

안전하고 신뢰할 수 있는 룸메이트 매칭 플랫폼 **ShareStay+** 의 프론트엔드 저장소입니다.  
React + TypeScript + Vite 기반으로 구축되었으며, MUI 디자인 시스템을 활용해 제품 UI를 구현합니다.

## 주요 화면

- **홈**: 서비스 소개, 추천 매물, 지역별 안전도 등 핵심 지표를 제공하는 랜딩 페이지
- **로그인/회원가입**: 새롭게 디자인된 인증 플로우 (이메일/비밀번호, Google OAuth)
- **방 찾기**: 필터, 정렬, 카드형 결과 목록이 포함된 검색 UI
- **이용방법**: ShareStay+ 사용 가이드, 단계별 안내
- **룸메이트 모집하기**: 매물 정보를 입력할 수 있는 폼 기반 화면
- **약관/개인정보/비밀번호 찾기** 등 정책 & 지원 관련 정적 페이지

## 기술 스택

- **React 18 + TypeScript**
- **Vite** 번들러 (HMR + 빠른 개발 환경)
- **Material UI (MUI)** 컴포넌트 라이브러리
- **React Router v6** 라우팅
- **react-hook-form + zod** 폼 상태/검증
- **Axios** API 통신 레이어
- **Google OAuth** 연동

## 실행 방법

```bash
pnpm install        # 또는 npm install, yarn install
pnpm run dev        # 개발 서버 실행 (http://localhost:5173)
pnpm run build      # 프로덕션 빌드
pnpm run preview    # 빌드 결과 미리보기
pnpm run lint       # ESLint 검사
```

> ⚠️  `VITE_GOOGLE_CLIENT_ID` 환경변수가 `.env` 혹은 실행 환경에 설정되어 있어야 Google 로그인 기능이 동작합니다.

## 폴더 구조 (발췌)

```
src/
├── auth/            # 인증 컨텍스트, 타입, 훅
├── components/      # 공통 UI 컴포넌트 (헤더, 푸터, 폼 필드 등)
├── lib/             # Axios 래퍼 등 유틸
├── pages/           # 각 라우트별 화면 컴포넌트
├── routes/          # 보호 라우터 등 라우팅 관련 코드
└── index.css        # 글로벌 스타일
```

## 라우팅

- `/` 홈
- `/login`, `/signup`
- `/rooms`, `/guide`, `/list-room`, `/safety-map`
- `/forgot-password`, `/terms`, `/privacy`
- `/profile`, `/admin/users` (보호 라우트)

`src/App.tsx`에서 전체 라우팅 구성을 확인할 수 있으며, 일부 페이지는 향후 백엔드 API 연동에 맞춰 확장됩니다.

## 디자인 & 브랜드 가이드

- 주요 색상: `#0c51ff` (Primary), `#f4f6fb` (Background)
- 타이포그래피: [@fontsource/roboto](https://fontsource.org/fonts/roboto)
- 공통 요소: 고정 상담 버튼, Sticky 헤더, 카드형 UI

## 향후 작업 예정

- 실데이터 연동 (매물 목록, 지도, 룸메이트 매칭 API)
- 검색/필터/폼 제출과 같은 상호작용에 대한 백엔드 연결
- 반응형/접근성 개선 및 E2E 테스트 도입

---
