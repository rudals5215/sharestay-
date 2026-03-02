# ShareStay

ShareStay는 공동 거주 및 방 공유를 목적으로 한 웹 서비스입니다.  
사용자는 방을 등록하고, 지도를 통해 방을 탐색하며,  
JWT 및 Google OAuth2 로그인을 통해 서비스를 이용할 수 있습니다.

이 프로젝트는 팀 프로젝트로 진행되었으며,  
프론트엔드–백엔드 인증 구조 연동과 협업 과정에서 발생한 문제 해결 경험을 중심으로 개발했습니다.

---

## 프로젝트 목적

- 실제 서비스와 유사한 구조의 팀 프로젝트 경험
- 프론트엔드와 백엔드 간 인증/인가 흐름 이해
- JWT / OAuth2 기반 로그인 구조 구현
- 협업 과정에서 발생하는 문제를 분석하고 해결하는 경험 축적

---

### 메인 페이지

![alt text](<팀 프로젝트 GIF/KakaoTalk_20260211_154835960-ezgif.com-video-to-gif-converter.gif>)

### 방찾기

![image](./팀%20프로젝트%20GIF/1-ezgif.com-video-to-gif-converter.gif)

### 필터 기능(1)

![alt text](<팀 프로젝트 GIF/2-ezgif.com-video-to-gif-converter.gif>)

### 필터 기능(2)

![alt text](<팀 프로젝트 GIF/3-ezgif.com-video-to-gif-converter.gif>)

### 방 등록

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (2).gif>)

### 방 상세보기

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (3).gif>)

### 지도

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (4).gif>)

### 지도(검색 기능)

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (5).gif>)

### 지도(미리 보기)

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (6).gif>)

### 지도(찜 기능)

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (7).gif>)

### 지도(축소, 확대)

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (8).gif>)

### 로그인

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (1).gif>)

### 회원 가입

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (10).gif>)

### 호스트 프로필

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (9).gif>)

### 게스트 프로필

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter.gif>)

### 관리자 대시보드(회원 관리)

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (회원 관리).gif>)

### 관리자 대시보드(회원 정지)

![alt text](<팀 프로젝트 GIF/ezgif.com-video-to-gif-converter (회원 정지).gif>)

---

## 🏛️ 아키텍처

> 프로젝트의 전체적인 구조입니다. 
> <img width="1499" height="658" alt="image" src="https://github.com/user-attachments/assets/f12e2e60-b5a5-4429-a349-8b2fc65ab418" />

> **Client (React)** ↔ **Backend (Spring Boot)** ↔ **Database (MariaDB)** / **Storage (Firebase)**

---

## 기술 스택

### Frontend

- React
- TypeScript
- MUI
- React Router
- Axios

### Backend

- Spring Boot
- Spring Security
- JWT
- OAuth2 (Google)

### Database

- MariaDB

### Collaboration

- Git / GitHub
- Velog

---

## 주요 기능 및 특징

- 인증/인가 (Authentication & Authorization)
  - JWT 기반 인증 : Access Token과 Refresh Token을 활용한 상태 비저장(Stateless) 로그인 구현
  - OAuth2 소셜 로그인 : Google 계정을 이용한 간편 로그인 기능 제공
  - Spring Security 기반 권한 관리: 사용자의 역할(GUEST, HOST, ADMIN)에 따른 API 접근 제어

- 방(Room) 관리 및 검색
  - RESTful API 설계: Spring Security와 연계하여 HOST 권한을 가진 사용자만 방 등록, 수정, 삭제가 가능하도록 API를 설계하고 구현
  - 지도 연동 검색 : 지도 상에서 위치 기반으로 매물을 탐색하고, 클러스터링으로 가시성 확보
  - 다중 조건 필터링 : 가격, 지역, 옵션 등 다양한 조건으로 매물 검색 기능 제공

- 파일 관리
  - Firebase Storage 연동 : 방 이미지 등 정적 파일을 업로드하고 CDN을 통해 제공

- 사용자 편의 기능
  - 즐겨찾기(찜): 로그인한 사용자가 관심있는 방을 저장하고, 마이페이지에서 목록을 관리하는 기능 구현
  - 공유하기: 클라이언트에서 현재 방의 고유 URL을 생성하여 클립보드에 복사하는 기능 제공

## 📖 API 명세

프로젝트의 모든 API 명세는 Swagger를 통해 확인하고 직접 테스트해볼 수 있습니다.

- Swagger UI: `[http://{서버_주소}/swagger-ui.html](http://localhost:8080/swagger-ui/index.html)`
<img width="1811" height="660" alt="스크린샷 2026-03-02 185119" src="https://github.com/user-attachments/assets/8a24a6a3-7fd2-4365-b8dd-19992b8db065" />
<img width="1782" height="782" alt="스크린샷 2026-03-02 185141" src="https://github.com/user-attachments/assets/e60ea7d5-708f-4461-903e-b2fa4ad36b27" />
<img width="1786" height="857" alt="스크린샷 2026-03-02 185200" src="https://github.com/user-attachments/assets/f0475b91-7911-4acc-89be-771d6693275c" />



---

## 인증 구조 요약

- 로그인 성공 시 JWT 발급
- OAuth2 로그인 성공 후 JWT를 프론트로 전달
- 프론트에서는 sessionStorage 기반으로 로그인 상태 관리
- 인증 구조 변경을 시도하며 발생한 문제들을 트러블슈팅으로 정리

> 인증 구조를 단순히 구현하는 것을 넘어,  
> 구조 변경 시 발생할 수 있는 문제와 롤백 결정까지 경험했습니다.

---

## Trouble Shooting

프로젝트 진행 중 다음과 같은 문제들을 겪었고,  
각각의 문제를 원인 분석 → 해결 → 선택의 이유 중심으로 정리했습니다.

- 인증 구조 불일치로 발생한 로그인 문제 (sessionStorage 기반 구조)
- OAuth2 로그인 후 리다이렉트가 동작하지 않던 이슈
- Git 협업 과정에서 발생한 충돌 및 fast-forward 이해
- 공유 링크 생성 로직을 프론트 중심으로 개선한 경험

자세한 내용은 Velog에 정리했습니다.

Velog 트러블슈팅 정리: [Velog](https://velog.io/@seojaeyeong-051/series/TeamProject-sharestay)

---

## 담당 역할

- 백엔드 인증/인가 구조 설계 및 구현
- OAuth2 로그인 흐름 구현
- 프론트–백엔드 인증 연동 문제 분석 및 해결
- 공유 기능 로직 개선 및 API 스펙 조율
- Git 충돌 해결 및 협업 브랜치 관리

---

## 회고

이 프로젝트를 통해 단순히 기능을 구현하는 것보다  
**구조의 일관성과 팀 상황을 고려한 판단**이 중요하다는 것을 배웠습니다.

특히 인증 구조 변경 과정에서  
이상적인 설계보다 현실적인 선택과 롤백 결정이  
프로젝트 완성도에 더 중요할 수 있다는 점을 체감했습니다.

---

## 실행 방법

### 사전 준비

- Git
- JDK 17
- Node.js (v18 이상)
- MariaDB

### Backend

1.  **저장소 클론 및 백엔드 폴더로 이동**

    ```bash
    git clone https://github.com/{your-repo-url}.git
    cd sharestay/sharestaybackend
    ```

2.  **데이터베이스 설정**
    - MariaDB에 `sharestay` 스키마를 생성합니다.
      ```sql
      CREATE DATABASE sharestay;
      ```
    - `src/main/resources/application.properties` 파일을 생성하고 아래 내용을 본인 환경에 맞게 수정합니다.
      ```properties
      spring.datasource.url=jdbc:mariadb://localhost:3306/sharestay
      spring.datasource.username=root
      spring.datasource.password=your_password
      ```

3.  **애플리케이션 실행**
    ```bash
    ./gradlew bootRun
    ```

### Frontend

```bash
npm install
npm run dev
```
