# 🧠 Re:Mind (AI 기반 정신건강 관리 플랫폼)

> **Re:Mind**는 AI 기술을 활용하여 사용자의 정신건강 상태를 다각도로 분석하고, 개인 맞춤형 리포트와 솔루션을 제공하는 통합 정신건강 관리 플랫폼입니다.

---

## 프로젝트 개요
현대인들의 정신건강 문제를 조기에 발견하고 관리할 수 있도록 돕기 위해 기획되었습니다. 사용자의 일기, 심리 검사(PHQ-9, GAD-7) 데이터를 AI가 분석하여 정기적인 리포트를 생성하며, 필요시 전문의(의사)와 연결할 수 있는 가교 역할을 수행합니다.

## 기술 스택
- **Language**: Java 21
- **Framework**: Spring Boot 4.0.4
- **Security**: Spring Security, JWT (JSON Web Token)
- **Database**: MySQL 8.0
- **Build Tool**: Gradle
- **Authentication**: Google OAuth2 (Android ID Token Verification)

## 핵심 구현 기능 (현재 완료)
### 안드로이드 중심 구글 로그인 및 인증 시스템
- **OAuth2 ID Token 검증**: 안드로이드 앱에서 발급받은 Google ID Token을 서버에서 직접 검증하는 보안 체계 구축.
- **JWT 기반 Stateless 서버**: 세션을 사용하지 않는 RESTful한 보안 구조로, 모바일 앱 환경에 최적화된 토큰 인증 방식 적용.
- **가입 여부 분기 처리**:
    - 기존 유저: 즉시 로그인 및 JWT 발급
    - 신규 유저: 404 응답을 통해 앱에서 추가 정보 입력(회원가입) 화면으로 유도
- **보안 강화**: `application.properties`의 민감 정보(API Key, Secret 등)를 환경 변수로 분리하여 GitHub 유출 방지.

## 📅 향후 개발 계획
- [ ] **AI 감정 일기**: 사용자가 작성한 일기를 분석하여 감정 상태 추출 및 시각화.
- [ ] **심리 검사 시스템**: PHQ-9(우울증), GAD-7(불안장애) 검사 결과 서버 저장 및 분석.
- [ ] **AI 리포트 생성**: 일기와 검사 결과를 종합하여 주간/월간 리포트 자동 생성.
- [ ] **의사-환자 연동 시스템**: 
    - 마이페이지 내 '의사 모드 전환' 기능.
    - 의사와 환자 간의 데이터 공유 및 수락/거절 프로세스.
- [ ] **챗봇 상담 서비스**: AI를 활용한 실시간 심리 상담 보조 기능.

## 테스트 가이드 (Postman)
현재 **개발용 테스트 모드**가 활성화되어 있습니다.
1. `POST /login` 또는 `POST /signup` 호출 시
2. `idToken` 값에 `"test-token"`을 입력하면 실제 구글 로그인 없이 테스트 이메일(`test@example.com`)로 로그인을 진행할 수 있습니다.
3. 인증이 필요한 API(예: `GET /my`) 호출 시, 발급받은 `accessToken`을 `Authorization: Bearer <TOKEN>` 헤더에 포함하여 전송하세요.

---
