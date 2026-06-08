# 🧠 Re:Mind (AI 기반 정신건강 관리 플랫폼)

> **Re:Mind**는 AI 기술을 활용하여 사용자의 정신건강 상태를 다각도로 분석하고, 개인 맞춤형 리포트와 솔루션을 제공하는 통합 정신건강 관리 플랫폼입니다.

---

## 프로젝트 개요
현대인들의 정신건강 문제를 조기에 발견하고 관리할 수 있도록 돕기 위해 기획되었습니다. 사용자의 일기, 심리 검사(PHQ-9, GAD-7) 데이터를 AI가 분석하여 정기적인 리포트를 생성하며, 전문의(의사)와 연결할 수 있는 가교 역할을 수행합니다.

### 스키마 설계 (ERD)
<img width="1407" height="1131" alt="Image" src="https://github.com/user-attachments/assets/e410c0ba-b535-496a-9ec7-716795d7e791" />

### 아키텍처 원칙
- **CQRS 패턴**: 서비스 계층을 Command(상태 변경)와 Query(조회)로 분리하여 유지보수성 강화.
- **RESTful API**: 리소스 중심의 설계와 복수형(Plural) 엔드포인트 규칙 준수.
- **전역 예외 처리**: `@RestControllerAdvice`를 통한 일관된 에러 응답 체계 구축.
- **성능 최적화**: Java 21 **Virtual Threads**를 활용한 비동기 작업 및 초고속 API 처리.

## 기술 스택
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.1
- **Security**: Spring Security, JWT (JSON Web Token), Google OAuth2
- **Database**: MySQL 8.0 / AWS RDS
- **Storage**: AWS S3 (의사 면허 인증 등)
- **AI Integration**: Python-based AI Servers (Chat/Report)

## 핵심 구현 기능
### 1. 사용자 인증 및 보안
- **통합 로그인/가입**: 단일 엔드포인트(`POST /users/login`)에서 구글 ID Token 검증과 자동 가입 동시 처리.
- **Soft Delete**: 회원 탈퇴 시 데이터를 즉시 삭제하지 않고 `deleted_at` 처리 및 이메일 마스킹을 통해 히스토리 보존 및 재가입 지원.

### 2. 정교한 감정 일기 (`/diaries`)
- **수면 시간 자동 보정**: 자정을 통과하는 수면 패턴에 대한 음수 수면 시간 방지 로직 내재화.
- **복약 관리**: 약물 복용 여부 및 반응 기록을 통해 리포트 분석 데이터 제공.
- **외부 스트레스 요인**: 당일 발생한 특이 사건(외부 스트레스) 기록 및 분석 연동.

### 3. AI 기반 지능형 서비스
- **AI 챗봇 상담 (`/ai/chat`)**: 사용자와의 실시간 대화를 통해 심리 상태 파악 및 위험 키워드(`is_risk`) 자동 감지.
- **의사 주도 AI 리포트 (`/ai/report`)**: 의사가 담당 환자의 데이터를 바탕으로 AI 심리 분석 리포트 생성. 
    - 일자별 감정/수면/복약/스트레스 데이터를 종합 분석하여 PHQ-9 예상 점수 및 치료 권고 사항 제공.

### 4. 전문적인 매칭 및 병원 데이터 (`/matches`, `/hospitals`)
- **의사-환자 매칭**: 1(의사):N(환자) 구조의 독립된 매칭 리소스 관리.
- **병원 데이터 고도화**: 공공데이터포털(심평원) API 연동 및 정제 로직을 통해 전국 정신건강의학과 병원 정보 수집 완료.

### 5. 심리 검사 시스템 (`/examinations`)
- **표준 척도 지원**: PHQ-9(우울), GAD-7(불안) 자동 채점 및 심각도 판정.

## 테스트 가이드 (Postman)
현재 **개발용 테스트 모드**가 활성화되어 있습니다.
1. `POST /users/login` 호출 시 `idToken`에 `"test_user"` 등을 입력하면 실제 구글 로그인 없이 테스트 계정으로 접속 가능합니다.
2. 인증이 필요한 API는 발급받은 `accessToken`을 `Authorization: Bearer <TOKEN>` 헤더에 포함하세요.
3. **Swagger UI**: `/swagger-ui/index.html`에서 모든 API 명세 확인 및 테스트가 가능합니다.

---
