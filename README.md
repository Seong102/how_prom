# 프롬프트 코딩 학습 플랫폼

> 초보 개발자가 LLM과의 대화형 프롬프트로 코드를 작성하는 법을 배우는 학습 플랫폼

---

## 🛠 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot 3.x, Spring Security, Spring Data JPA |
| Database | MySQL 8.0 |
| Frontend | Thymeleaf, Vanilla JS |
| 인증 | JWT (jjwt) |
| LLM 연동 | WebClient (Claude / GPT API) |

---

## 📁 프로젝트 구조

```
src/main/java/com/promptcode/
├── auth/           # 회원가입, 로그인, JWT
├── problem/        # 문제 목록, 상세
├── chat/           # 대화형 LLM 호출
├── submission/     # 제출, LLM 평가, 채점
├── community/      # 다른 풀이 보기, 좋아요
├── mypage/         # 내 제출 기록
├── admin/          # 문제 등록, 관리, 통계
└── common/         # 공통 설정, 보안 필터
```

---

## 🌿 브랜치 전략

```
main
 └── develop
      ├── feature/기능명
      └── fix/버그명
```

| 브랜치 | 용도 | 병합 대상 |
|--------|------|-----------|
| `main` | 최종 배포 | — |
| `develop` | 개발 통합 | `main` |
| `feature/기능명` | 기능 개발 | `develop` |
| `fix/버그명` | 버그 수정 | `develop` |

### 브랜치 네이밍 규칙

```
feature/SCR-PROB-02-chat-ui      # 화면 ID 기준
feature/llm-evaluation-service   # 기능명 기준
fix/budget-token-overflow        # 버그 수정
```

---

## 🔀 PR & 코드 리뷰 규칙

- `feature` → `develop` : PR 필수, **1명 이상 Approve** 후 머지
- `develop` → `main` : PR 필수, **전원 Approve** 후 머지
- PR 제목 형식: `[FEAT] 기능명` / `[FIX] 버그명` / `[REFACTOR] 내용`
- PR 생성 시 템플릿의 **구현 검증 항목 체크리스트** 반드시 작성

---

## 📋 이슈 라벨

| 라벨 | 설명 |
|------|------|
| `bug` | 버그 리포트 |
| `enhancement` | 기능 요청 |
| `task` | 개발 작업 단위 |
| `in-progress` | 작업 진행 중 |
| `review` | 리뷰 요청 |

---

## ⚙️ 로컬 실행 방법

### 1. 레포 클론
```bash
git clone https://github.com/{org}/{repo}.git
cd {repo}
```

### 2. 환경변수 설정
`src/main/resources/application.properties` 파일 생성:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/prompt_platform
spring.datasource.username=root
spring.datasource.password=비밀번호

llm.base-url=https://api.anthropic.com
llm.api-key=발급받은_API_KEY
llm.model=claude-sonnet-4-6
llm.system-prompt-tokens=42
```

### 3. DB 생성
```bash
mysql -u root -p < database/schema.sql
```

### 4. 실행
```bash
./mvnw spring-boot:run
```

접속: http://localhost:8080

---

## 👥 팀원

| 이름 | 담당 영역 | GitHub |
|------|-----------|--------|
|  |  |  |
|  |  |  |
|  |  |  |
