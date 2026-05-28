# HowProm 기능 설계서

---

## 프로젝트 역할 분담

```
[김성백]
메인 대시보드
회원가입/로그인
문제 목록

[박세준]
문제 풀기 + 로컬LLM

[전기현]
다른 풀이 보기
채점 결과
마이페이지

[김은빈]
문제 등록
문제 관리
통계 대시보드
```

## 1. 시스템 개요

### 1.1 서비스 정의
**HowProm**은 사용자가 로컬 LLM(Ollama)과 대화하며 Java 코드를 작성하고, 작성한 코드를 요구사항 기준으로 LLM이 자동 채점하는 **프롬프트 엔지니어링 학습 플랫폼**이다.

### 1.2 핵심 차별점
- 사용자가 직접 코드를 작성하는 대신 **AI에게 좋은 프롬프트를 작성**해 코드를 끌어낸다.
- 평가 유형이 3가지(정확도/효율성/토큰 절약)로 나뉘어, 단순 정답뿐 아니라 **프롬프트 효율성**까지 학습한다.
- 통과한 문제에 한해 **다른 사용자의 풀이를 열람**할 수 있다.

### 1.3 사용자 역할
| 역할 | 권한 |
|---|---|
| USER | 문제 풀이, 제출, 채점 결과 열람, 다른 풀이 열람, 프로필 수정 |
| ADMIN | USER의 모든 권한 + 문제 등록/수정/삭제, 통계 대시보드 열람 |

### 1.4 기술 스택
| 영역 | 기술 |
|---|---|
| 백엔드 | Spring Boot, Spring Security, Spring Data JPA |
| 뷰 | Thymeleaf (서버 사이드 렌더링) |
| DB | MySQL 8.0+ (utf8mb4) |
| 인증 | Spring Security + BCrypt + Remember-Me (Hash-Based, 14일) |
| LLM | Ollama (로컬) — WebClient(WebFlux) 연동 |
| 코드 실행 | 로컬 javac/java 프로세스 (ProcessBuilder) |
| 이메일 | Gmail SMTP (임시 비밀번호 발송) |
| 배포 | WAR (ServletInitializer) |

---

## 2. 화면 목록 (Screen Map)

| 코드 | 화면명 | URL | 접근 권한 | 템플릿 |
|---|---|---|---|---|
| SCR-MAIN | 메인 대시보드 | `/`, `/main` | 전체 | main/main.html |
| SCR-AUTH-01 | 로그인 | `/auth/login` | 비로그인 | auth/login.html |
| SCR-AUTH-02 | 회원가입 | `/auth/signup` | 비로그인 | auth/signup.html |
| SCR-AUTH-03 | 비밀번호 찾기 | `/auth/find-password` | 비로그인 | auth/find-password.html |
| SCR-PROB-01 | 문제 목록 | `/problems` | 전체 | problem/problemList.html |
| SCR-PROB-02 | 문제 풀이 (채팅) | `/problems/{id}` | 로그인 | problem/problemDetail.html |
| SCR-PROB-03 | 채점 결과 | `/result/{submissionId}` | 로그인 | problem/result.html |
| SCR-MY-01 | 마이페이지 | `/mypage` | 로그인 | mypage/mypage.html |
| SCR-MY-02 | 내 풀이 상세 | `/submission/{id}/review` | 로그인 | submission/review.html |
| SCR-COM-01 | 다른 풀이 목록 | `/community/problems/{problemId}` | 로그인 | community/community.html |
| SCR-COM-02 | 다른 풀이 상세 | `/community/submissions/{submissionId}` | 로그인 | submission/review.html |
| SCR-ADM-01 | 관리자 통계 대시보드 | `/admin` | ADMIN | admin/AdminDashboard.html |
| SCR-ADM-02 | 문제 관리 | `/admin/problems/manage` | ADMIN | admin/problems/manage.html |
| SCR-ADM-03 | 문제 등록/수정 | `/admin/problems/register`, `/edit/{id}` | ADMIN | admin/problems/register.html |
| SCR-ERR-403 | 권한 없음 | `/error/403` | 전체 | error/403.html |
| (공통) | 프로필 수정 모달 | (헤더 내장) | 로그인 | common/header.html |

---

## 3. 기능 상세

### 3.1 인증 (Authentication)

#### 3.1.1 회원가입 [SCR-AUTH-02]
- **입력**: 이메일, 별명(2~12자), 비밀번호(8자 이상), 비밀번호 확인
- **검증** (Bean Validation + Service):
  - 이메일 형식 (`@Email`)
  - 별명 길이 (`@Size(2,12)`)
  - 비밀번호 길이 (`@Size(min=8)`)
  - 비밀번호 == 비밀번호 확인 (Service에서 검증)
  - 이메일 중복 (DB 조회)
  - 별명 중복 (DB 조회)
- **처리**: BCrypt로 비밀번호 해싱 → `role=USER`로 저장
- **실시간 이메일 중복 확인**: `GET /auth/check-email?email=` (signup.js에서 AJAX 호출)
- **성공 후**: `/auth/login?signup`으로 리다이렉트
- **실패 시**: `/auth/signup?error={메시지}`로 리다이렉트 (URL 인코딩)

#### 3.1.2 로그인 [SCR-AUTH-01]
- **입력**: 이메일(username), 비밀번호, 로그인 유지(체크박스)
- **처리 경로**: Spring Security가 `POST /login` 가로채서 처리
- **핵심 설정**:
  - `usernameParameter("email")` — 이메일을 username으로 사용
  - 성공 시 `/`로 리다이렉트
  - 실패 시 `/auth/login?error`
- **로그인 유지(Remember-Me)**: 체크 시 Hash-Based 토큰 쿠키 발급, 14일 유지
- **하단 링크**: "회원가입" → `/auth/signup`, "비밀번호 찾기" → `/auth/find-password`

#### 3.1.3 비밀번호 찾기 [SCR-AUTH-03]
- **입력**: 이메일
- **처리**:
  1. 해당 이메일 사용자 조회 (없어도 동일 응답 — 이메일 enumeration 방지)
  2. 임시 비밀번호 10자 생성 (영문 대소문자 + 숫자, SecureRandom)
  3. BCrypt 해싱하여 DB 업데이트
  4. Gmail SMTP로 임시 비밀번호 발송
- **성공/실패 무관**: `/auth/find-password?sent`로 리다이렉트
- **로그아웃**: `POST /logout` → `/`로 이동, Remember-Me 쿠키도 삭제

#### 3.1.4 권한 기반 접근 제어
- **공개 경로** (permitAll): `/`, `/main`, `/problems`, `/auth/**`, 정적 리소스, `/error/**`
- **ADMIN 전용**: `/admin/**`
- **인증 필요**: `/profile/**`, 그 외 모든 경로
- **비인증 사용자가 보호 페이지 접근 시**: `/`(메인)로 리다이렉트 (로그인 페이지가 아님)
- **권한 부족(USER가 ADMIN 페이지)**: `/error/403` 커스텀 페이지

---

### 3.2 메인 대시보드 [SCR-MAIN]

화면은 5개 영역으로 구성되며, 로그인 여부에 따라 노출이 달라진다.

| 영역 | 비로그인 | 로그인 | 데이터 소스 |
|---|---|---|---|
| 환영 메시지 | CTA형 (가입 유도) | 시간대별 인사 + 닉네임 | 서버 시각 / Principal |
| 이어서 풀기 | 숨김 | 노출 | 미완료 최근 제출 |
| 추천 문제 | 숨김 | 노출 | 평가 유형별 미PASSED 최신 |
| 새로 등록된 문제 | 노출 | 노출 | 최근 7일, 최대 5개 |
| 이번 주 우수 풀이 | 노출 | 노출 | 이번 주 PASSED 상위 5개 |

#### 3.2.1 시간대별 인사
- 06~12시: "좋은 아침이에요"
- 12~18시: "오후도 화이팅이에요"
- 18~24시: "오늘도 수고하셨어요"
- 00~06시: "야간 학습 중이시군요"
- 형식: `{메시지}, {닉네임}님`

#### 3.2.2 이어서 풀기
- **정의**: PASSED 받지 못한 문제 중 가장 최근에 시도한 1건
- **쿼리 로직**: 사용자가 PASSED한 problem_id를 제외하고, 남은 제출 중 `submitted_at` 최신순 1건
- **표시**: 문제 제목, 평가 유형 배지, 마지막 상태(채점 중/컴파일 실패·재도전 가능/시스템 오류)
- **없을 때**: "아직 풀어본 문제가 없어요" 빈 상태 UI

#### 3.2.3 추천 문제
- **정의**: 3개 평가 유형(STANDARD/EFFICIENCY/BUDGET)별로 PASSED 못 받은 최신 문제 1개씩
- **쿼리**: 유형별로 `findRecommendedByType` 3회 호출
- **표시**: 유형별로 요구사항 개수, EFFICIENCY는 평균 토큰, BUDGET은 토큰 상한
- **없을 때**: "모든 문제를 다 풀었어요" 메시지

#### 3.2.4 새로 등록된 문제
- **정의**: 공개 문제 중 최근 7일 이내 등록, 최대 5개, 최신순
- **표시**: 평가 유형, 제목, 등록일(상대 날짜: "오늘"/"어제"/"N일 전")
- **버튼**: 로그인 시 풀이 페이지로, 비로그인 시 confirm으로 로그인 유도

#### 3.2.5 이번 주 우수 풀이
- **정의**: 이번 주(월 00:00 ~ 다음 주 월 00:00) PASSED 제출 중 점수 상위 5개
- **정렬**: 점수 내림차순 → 토큰 오름차순 → 제출 시각 오름차순
- **버튼 분기** (로그인):
  - 본인이 그 문제를 PASSED함 → "풀이 보기" → `/result/{submissionId}`
  - 본인이 안 풂 → "풀러 가기" → 문제 풀이 페이지
- **비로그인**: confirm으로 로그인 유도

---

### 3.3 문제 목록 [SCR-PROB-01]

#### 3.3.1 필터 및 정렬
- **평가 유형 탭**: 전체 / STANDARD / EFFICIENCY / BUDGET
- **풀이 상태 필터** (로그인만): 전체 / 안 푼 문제 / PASSED 받은 문제
- **정렬**: 최신순(기본) / 오래된순
- **페이지네이션**: 페이지당 20개 (`PAGE_SIZE` 상수)

#### 3.3.2 조회 분기
- **비로그인**: JPQL `findPublicListForGuest` — status/bestScore는 null
- **로그인**: Native query `findPublicListForUser`
  - 정책 A 반영: PASSED 있으면 'PASSED', 없으면 최근 제출 상태
  - 최고 점수: PASSED 중 MAX(score)
  - HAVING 절로 풀이 상태 필터링

#### 3.3.3 상태 표시 (로그인만)
| 상태 | 아이콘 |
|---|---|
| PASSED | ✓ |
| FAILED | ✗ |
| GRADING | ⏱ |
| 미도전 | - |

- **버튼**: PASSED는 "다시 풀기"(outline), 그 외 "풀러 가기"(fill)
- **비로그인**: 제목 링크 + 버튼 모두 confirm으로 로그인 유도

---

### 3.4 문제 풀이 [SCR-PROB-02]

#### 3.4.1 화면 구성
- 문제 정보 (제목, 설명, 예시 입출력, 평가 유형, 요구사항 목록)
- AI 채팅 영역 (사용자 ↔ LLM 대화)
- 코드 에디터
- 코드 실행(테스트) 영역
- 제출 버튼

#### 3.4.2 AI 채팅 — `POST /api/chat/message`
- **요청**: `{ problemId, messages: [{role, content}], prevTotalTokens }`
- **처리**:
  1. system prompt(고정) + 사용자 대화 이력으로 messages 구성
  2. Ollama `/api/chat` 호출 (stream=false)
  3. 응답에서 content 추출
  4. **이번 턴 사용자 토큰 계산**: `prompt_eval_count - systemPromptTokens - prevTotalTokens`
- **응답**: `{ content, promptTokens(이번 턴), assistantTokens(AI 답변) }`
- **토큰 누적**: 프론트가 assistantTokens로 prevTotalTokens 누적 관리

#### 3.4.3 코드 실행(테스트) — `POST /api/code/run`
- **요청**: `{ code, stdin }`
- **처리**:
  1. 임시 디렉토리 생성 (`howprom_xxx`)
  2. `Main.java` 작성
  3. 컴파일 (`javac`, 최대 10초)
     - 실패 → compileError 반환
  4. 실행 (`java -Xmx128m`, 최대 5초)
     - stdin 전달
     - stdout/stderr 별도 스레드로 동시 읽기 (데드락 방지)
     - 타임아웃 시 강제 종료
  5. 임시 디렉토리 정리 (finally)
- **응답**: `{ stdout, compileError, runtimeError }` (정적 팩토리로 셋 중 하나만 채움)
- **출력 제한**: 최대 10,000자 (초과 시 절단)
- **런타임 에러**: JVM 내부 스택(java./sun./jdk.) 제거하고 사용자 코드 부분만 표시

#### 3.4.4 코드 제출 — `POST /api/submissions`
→ 3.5 자동 채점 참조

---

### 3.5 자동 채점 (핵심 기능)

#### 3.5.1 제출 처리 흐름 — `SubmissionService.submit()`
```
1. 문제 조회 (findByIdWithRequirements — 요구사항 JOIN FETCH)
2. finalCode 존재 검증 (없으면 예외)
3. BUDGET 모드 토큰 상한 재검증 (서버 사이드)
4. LLM 채점 호출 (LlmGradingService.grade)
5. 평가 유형별 최종 점수 계산
6. status 결정 (PASSED/FAILED/ERROR)
7. submissions INSERT (graded_at 기록)
8. EFFICIENCY 모드면 avg_user_tokens 갱신
9. submissionId 반환
```

#### 3.5.2 LLM 채점 — `LlmGradingService.grade()`
- **입력**: 문제 제목, 설명, 요구사항 목록, 최종 코드
- **system prompt** (채점 전용):
  - JSON 형식만 반환 강제
  - 이전 대화/다른 문제 무시
  - 각 요구사항: 로직 존재하면 weight 만점, 없으면 0점
  - score >= 60이면 passed=true
- **Ollama 옵션** (결정적 채점):
  - `temperature=0.0` (편차 최소화)
  - `seed=42` (재현성)
  - `keep_alive=0` (세션 즉시 해제, 컨텍스트 오염 방지)
- **이전 대화 이력 제외**: 채점 메시지만 전달 (이전 문제 문맥 오염 방지)
- **재시도**: 1회 실패 시 1회 재시도, 2회 실패 → ERROR
- **JSON 추출**: ```json``` 코드블록 감싸짐 대비 파싱
- **총점 계산**: LLM 반환 총점 무시, **요구사항별 점수를 서버에서 직접 합산** (환각 방어)
- **결과**: `GradingResult { score, passed, feedback, requirementsResult, isError }`

#### 3.5.3 평가 유형별 최종 점수 — `calcFinalScore()`
| 유형 | 계산식 |
|---|---|
| STANDARD | LLM 채점 점수 그대로 |
| EFFICIENCY | `점수×정확도가중치 + 점수×효율성×효율성가중치`<br>효율성 = `max(0, 1 - 사용토큰/평균토큰)`<br>(첫 제출자는 평균이 0이라 LLM 점수 그대로) |
| BUDGET | LLM 채점 점수 그대로 (토큰 상한은 제출 시 사전 검증) |

#### 3.5.4 status 결정
| 조건 | status | score |
|---|---|---|
| LLM 호출/파싱 실패 | ERROR | 0 |
| 채점 60점 이상 | PASSED | 0~100 |
| 채점 60점 미만 | FAILED | 0~100 |

#### 3.5.5 EFFICIENCY 평균 토큰 갱신
- ERROR 제외한 해당 문제 전체 제출의 `total_user_tokens` 평균 계산
- `problems.avg_user_tokens` 갱신
- 다음 제출자의 효율성 점수 계산 기준이 됨

---

### 3.6 채점 결과 [SCR-PROB-03]

#### 3.6.1 결과 조회 — `GET /result/{submissionId}`
- **표시**: 문제명, 평가 유형, 점수, 상태, 사용 토큰, 대화 턴 수
- **요구사항별 결과**: 각 요구사항 설명, 획득 점수, 만점, 달성률(%)
  - 달성률 = `획득점수 × 100 / weight`

#### 3.6.2 채점 상태 폴링 — `GET /api/submissions/{submissionId}/status`
- **목적**: GRADING 상태일 때 페이지 새로고침 없이 상태 확인
- **응답**: `{ "status": "PASSED" }` 등 status 문자열만
- **용도**: 프론트가 일정 간격으로 호출하여 채점 완료 감지

---

### 3.7 마이페이지 [SCR-MY-01]

#### 3.7.1 화면 구성 — `GET /mypage`
- **상단 통계 카드 3개**:
  - 총 제출 수
  - 통과 문제 수 (PASSED, 중복 제거)
  - 통과율 (PASSED 건수 / 총 제출 수 × 100, 소수 1자리)
- **하단 제출 기록 테이블**: 페이지당 5개
  - 문제명, 평가 유형, 점수, 사용 토큰, 제출일, 결과(상태 배지)
  - 행 클릭 → `/result/{submissionId}`

#### 3.7.2 성능 최적화
- 목록 조회 시 `conversation`(JSON 대용량) 컬럼 제외하고 DTO 프로젝션
- 통계는 별도 count 쿼리로 집계

#### 3.7.3 REST API (향후 SPA 대비)
- `GET /api/mypage` — 통계 + 페이징 목록 (JSON)
- `GET /api/mypage/submissions/{id}` — 단건 상세 (소유권 검증)

---

### 3.8 내 풀이 상세 [SCR-MY-02]

#### 3.8.1 조회 — `GET /submission/{submissionId}/review`
- **소유권 검증**: 본인 제출이 아니면 SecurityException
- **화면 구성** (2열):
  - 좌: 대화 이력 (user/assistant 말풍선)
  - 우: 채점 결과 요약 (점수, 사용 토큰, 대화 턴 수) + 최종 코드

---

### 3.9 커뮤니티 (다른 풀이 보기)

#### 3.9.1 다른 풀이 목록 [SCR-COM-01] — `GET /community/problems/{problemId}`
- **정의**: 특정 문제의 PASSED 제출 목록
- **정렬**: 점수 내림차순 → 토큰 오름차순
- **표시**: 순위 배지(1~3위 메달), 닉네임, 점수, 사용 토큰, 제출일
- **"보기" 버튼** → `/community/submissions/{submissionId}`

#### 3.9.2 다른 풀이 상세 [SCR-COM-02] — `GET /community/submissions/{submissionId}`
- **소유권 검증 없음** (공개 열람)
- 내 풀이 상세와 동일 템플릿(`submission/review.html`) 재사용

---

### 3.10 프로필 수정 (모달)

#### 3.10.1 닉네임 변경 — `POST /profile/nickname`
- **입력**: 새 닉네임 (2~16자)
- **검증**: 본인 닉네임과 같으면 무시, 중복이면 예외
- **세션 갱신**: SecurityContext의 Principal 객체를 새 닉네임으로 교체
- **응답**: `{ success, nickname }` 또는 `{ success: false, message }`
- **클라이언트**: 헤더의 닉네임 DOM 즉시 갱신

#### 3.10.2 비밀번호 변경 — `POST /profile/password`
- **입력**: 현재 비밀번호, 새 비밀번호(8자 이상), 새 비밀번호 확인
- **검증**:
  - 현재 비밀번호 일치 (BCrypt matches)
  - 새 비밀번호 == 확인
  - 새 비밀번호 ≠ 현재 비밀번호 (의미 없는 변경 방지)
- **응답**: `{ success }` 또는 `{ success: false, message }`

---

### 3.11 관리자 기능

#### 3.11.1 통계 대시보드 [SCR-ADM-01] — `GET /admin`
- **전체 통계**: 총 제출 수, 총 통과율, 활성 사용자 수, 공개/전체 문제 수
- **문제별 통계 테이블**: 제출 수, 평균 점수, PASSED/FAILED/ERROR 건수
- **효율성 차트**: EFFICIENCY 문제별 평균 토큰 (막대 너비 비율)
- **요구사항 분석**: 요구사항별 평균 달성도, 실패율 (JSON_TABLE로 분해 집계)
- **최근 제출 현황**: GRADING 제외 최근 10건
- **문제별 최고 점수 랭킹**: ROW_NUMBER() OVER PARTITION으로 문제당 1등

#### 3.11.2 문제 관리 [SCR-ADM-02] — `GET /admin/problems/manage`
- **목록**: 전체 문제 + 검색(번호/제목)
- **각 문제**: 통계(통과율 등) 표시
- **액션**: 등록 / 수정 / 삭제 / 공개·비공개 토글

#### 3.11.3 문제 등록/수정 [SCR-ADM-03]
- **등록** — `POST /admin/problems/register`:
  - 입력: 제목, 설명, 평가 유형, 공개 여부, 예시 입출력, 토큰 상한, 요구사항 목록
  - **요구사항 배점 합계 = 100점 검증** (프론트: 부동소수점 반올림 처리)
  - 평가 유형별 가중치 자동 설정 (EFFICIENCY: 0.7/0.3, 그 외 1.0/0.0)
  - 출제자: 현재 로그인 관리자 (`@AuthenticationPrincipal`)
- **수정** — `POST /admin/problems/edit/{id}`:
  - 기존 요구사항 전체 삭제 후 재조립
- **삭제** — `POST /admin/problems/delete/{id}`:
  - 공개 상태면 거부
  - **제출 이력 있으면 거부** (학습 데이터 보존)
- **공개 토글** — `POST /admin/problems/toggle/{id}`

---

## 4. 데이터 모델 요약

### 4.1 테이블
| 테이블 | 핵심 컬럼 |
|---|---|
| users | id, email(uk), password(BCrypt), nickname(uk), role(USER/ADMIN), created_at |
| problems | id, title, description, evaluation_type, token_limit, correctness_weight, efficiency_weight, avg_user_tokens, is_public, created_by(FK), created_at, updated_at |
| requirements | id, problem_id(FK), description, weight, display_order |
| submissions | id, user_id(FK), problem_id(FK), conversation(JSON), final_code, total_user_tokens, completion_tokens, score, requirements_result(JSON), status, submitted_at, graded_at |

### 4.2 주요 ENUM
- `UserRole`: USER, ADMIN
- `EvaluationType`: STANDARD, EFFICIENCY, BUDGET
- `SubmissionStatus`: GRADING(채점 중), PASSED(60점 이상), FAILED(60점 미만), ERROR(채점 오류)

### 4.3 FK 정책
| 관계 | ON DELETE |
|---|---|
| submissions.problem_id → problems.id | RESTRICT |
| submissions.user_id → users.id | RESTRICT |
| problems.created_by → users.id | RESTRICT |
| requirements.problem_id → problems.id | CASCADE |

### 4.4 정책 A (사용자 상태 판정)
같은 문제에 여러 제출 시:
1. PASSED 있으면 → 최고 score 제출
2. PASSED 없으면 → 최근 제출
3. 제출 없으면 → 미도전

---

## 5. API 목록

### 5.1 화면 렌더링 (Controller → Thymeleaf)
| Method | URL | 설명 |
|---|---|---|
| GET | `/`, `/main` | 메인 대시보드 |
| GET | `/auth/login` | 로그인 페이지 |
| GET | `/auth/signup` | 회원가입 페이지 |
| GET | `/auth/find-password` | 비밀번호 찾기 페이지 |
| GET | `/problems` | 문제 목록 |
| GET | `/problems/{id}` | 문제 풀이 |
| GET | `/result/{submissionId}` | 채점 결과 |
| GET | `/mypage` | 마이페이지 |
| GET | `/submission/{id}/review` | 내 풀이 상세 |
| GET | `/community/problems/{problemId}` | 다른 풀이 목록 |
| GET | `/community/submissions/{submissionId}` | 다른 풀이 상세 |
| GET | `/admin` | 관리자 대시보드 |
| GET | `/admin/problems/manage` | 문제 관리 |
| GET | `/admin/problems/register` | 문제 등록 폼 |
| GET | `/admin/problems/edit/{id}` | 문제 수정 폼 |
| GET | `/error/403` | 권한 없음 |

### 5.2 폼 제출 (Spring Security / POST)
| Method | URL | 설명 |
|---|---|---|
| POST | `/login` | 로그인 처리 (Security) |
| POST | `/logout` | 로그아웃 (Security) |
| POST | `/auth/signup` | 회원가입 처리 |
| POST | `/auth/find-password` | 비밀번호 찾기 처리 |
| POST | `/admin/problems/register` | 문제 등록 |
| POST | `/admin/problems/edit/{id}` | 문제 수정 |

### 5.3 REST API (JSON)
| Method | URL | 설명 |
|---|---|---|
| GET | `/auth/check-email` | 이메일 중복 확인 |
| POST | `/api/chat/message` | AI 채팅 |
| POST | `/api/code/run` | 코드 실행 |
| POST | `/api/submissions` | 코드 제출/채점 |
| GET | `/api/submissions/{id}/status` | 채점 상태 폴링 |
| POST | `/profile/nickname` | 닉네임 변경 |
| POST | `/profile/password` | 비밀번호 변경 |
| POST | `/admin/problems/delete/{id}` | 문제 삭제 |
| POST | `/admin/problems/toggle/{id}` | 공개 토글 |
| GET | `/api/mypage` | 마이페이지 데이터(SPA용) |
| GET | `/api/mypage/submissions/{id}` | 제출 상세(SPA용) |

---

## 6. 보안 설계

| 항목 | 구현 |
|---|---|
| 비밀번호 저장 | BCrypt 해싱 (매번 다른 salt) |
| 인증 방식 | Spring Security 폼 로그인 + 세션 |
| 자동 로그인 | Remember-Me (Hash-Based, 14일) |
| 권한 제어 | URL 패턴 매칭 + `@PreAuthorize` + Thymeleaf `sec:authorize` |
| 제출 소유권 | 내 풀이 상세 조회 시 user_id 검증 |
| 이메일 enumeration 방지 | 비밀번호 찾기 시 사용자 유무 무관 동일 응답 |
| CSRF | 기본 활성, `/profile/**`만 예외 (AJAX) |
| BUDGET 토큰 검증 | 프론트 + 서버(제출 시) 이중 검증 |

---

## 7. 향후 보완 과제

| 항목 | 내용 |
|---|---|
| 컴파일-채점 연계 | 현재 코드 실행과 채점이 분리됨. 제출 시 컴파일 검증을 채점 직전에 통합 가능 |
| 코드 실행 격리 | 로컬 프로세스 실행 → Docker/SecurityManager 격리 (공개 서비스 시) |
| 민감정보 분리 | DB/메일 비밀번호를 환경변수로 분리 |
| 임시 비밀번호 만료 | 현재 만료 시간 없음 → 유효시간 추가 |
| systemPromptTokens 자동화 | 현재 properties에 수동 입력 → tokenizer 자동 계산 |
| 부분 점수 채점 | 현재 요구사항별 만점/0점 → 부분 점수 도입 검토 |

---

## 8. 패키지 구조

```
com.howprom/
├── HowPromApplication / ServletInitializer
├── admin/          (controller, dto, service) — 관리자
├── auth/           (AuthController, EmailService) — 인증
├── chat/           (controller, dto, service) — AI 채팅
├── code/           (controller, dto, service) — 코드 실행
├── common/
│   ├── controller/ (Error403Controller)
│   └── entity/     (User, Problem, Requirement, Submission, 3개 enum)
├── community/      (controller, dto, service) — 다른 풀이
├── config/         (WebClientConfig)
├── main/           (controller, dto, service) — 대시보드
├── problem/        (controller, dto, service) — 문제 목록/상세/결과
├── repository/     (5개 Repository 집결)
├── security/       (SecurityConfig, PasswordConfig)
├── submission/     (controller, dto, service) — 제출/채점/마이페이지
└── user/           (CustomUserPrincipal, ProfileController, dto, service)
```

---
