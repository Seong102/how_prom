-- =====================================================
--  HowProm - 프롬프트 코딩 학습 플랫폼 DB 생성 스크립트
--  Version : v2.2
--  DBMS    : MySQL 8.0+
--  문자셋  : utf8mb4 / utf8mb4_unicode_ci
--  테이블  : users / problems / requirements / submissions
--
--  v1.0 → v2.2 변경 이력
--    [users]
--      + nickname (UNIQUE) 추가 - 다른 풀이 열람 시 작성자 표시용
--    [problems]
--      + created_by (FK) 추가 - 출제자 추적, 관리자 통계용
--      ~ avg_prompt_tokens → avg_user_tokens 로 명칭 변경 (v2.2)
--    [submissions]
--      + completion_tokens 추가 - LLM 출력 토큰 (참고/통계용)
--      + graded_at 추가 - 비동기 채점 완료 시점
--      + final_code 추가 - 최종 제출 코드 본문 (v2.2)
--      ~ status ENUM에 GRADING 추가 - 채점 중 상태
--      ~ created_at → submitted_at 으로 명칭 변경 (의미 명확화)
--      ~ prompt_tokens → total_user_tokens 로 명칭 변경 (v2.2)
--
--    v2.0 → v2.1: category 컬럼 제거 (YAGNI - 필요 시점에 추가)
--    v2.1 → v2.2: final_code 추가, 토큰 관련 컬럼 명칭 통일
-- =====================================================

-- 1) 데이터베이스 생성 -------------------------------------------
DROP DATABASE IF EXISTS how_prom;

CREATE DATABASE how_prom
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE       utf8mb4_unicode_ci;

USE how_prom;


-- 2) users -------------------------------------------------------
CREATE TABLE users (
    id           BIGINT       NOT NULL AUTO_INCREMENT             COMMENT '사용자 고유 ID',
    email        VARCHAR(255) NOT NULL                             COMMENT '로그인 이메일',
    password     VARCHAR(255) NOT NULL                             COMMENT 'BCrypt 해시값',
    nickname     VARCHAR(50)  NOT NULL                             COMMENT '화면 표시용 닉네임 (다른 풀이 열람 시 노출)',
    role         ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER'      COMMENT 'USER: 일반 / ADMIN: 관리자',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '가입 일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email    (email),
    UNIQUE KEY uk_users_nickname (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자 계정 및 역할 (USER / ADMIN)';


-- 3) problems ----------------------------------------------------
CREATE TABLE problems (
    id                   BIGINT       NOT NULL AUTO_INCREMENT      COMMENT '문제 고유 ID',
    title                VARCHAR(255) NOT NULL                     COMMENT '문제 제목',
    description          TEXT         NOT NULL                     COMMENT '문제 설명',
    example_input        TEXT         NULL                         COMMENT '예시 입력값',
    example_output       TEXT         NULL                         COMMENT '예시 출력값',
    evaluation_type      ENUM('STANDARD','EFFICIENCY','BUDGET') NOT NULL COMMENT '평가 유형',
    token_limit          INT          NULL                         COMMENT 'BUDGET 모드 토큰 상한 (다른 모드는 NULL)',
    correctness_weight   FLOAT        NOT NULL DEFAULT 0.7         COMMENT 'EFFICIENCY 정확도 가중치',
    efficiency_weight    FLOAT        NOT NULL DEFAULT 0.3         COMMENT 'EFFICIENCY 효율성 가중치',
    avg_user_tokens      FLOAT        NOT NULL DEFAULT 0           COMMENT 'EFFICIENCY 기준값 - 제출들의 total_user_tokens 평균 (실시간 갱신)',
    is_public            TINYINT(1)   NOT NULL DEFAULT 0           COMMENT '0=비공개, 1=공개',
    created_by           BIGINT       NOT NULL                     COMMENT '출제자 (users.id, 관리자만 가능)',
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록 일시',
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                              ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (id),

    -- 인덱스: 문제 목록 필터링 및 관리자 통계용
    KEY idx_problems_filter  (is_public, evaluation_type),
    KEY idx_problems_creator (created_by),

    -- 출제자 FK: 관리자 계정 삭제 방지 (제출 기록과 함께 보존)
    CONSTRAINT fk_problems_creator
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    -- CHECK 제약 (MySQL 8.0.16+)
    CONSTRAINT chk_problems_weight_sum
        CHECK (ABS(correctness_weight + efficiency_weight - 1.0) < 0.001),
    CONSTRAINT chk_problems_budget_token
        CHECK (evaluation_type <> 'BUDGET' OR token_limit IS NOT NULL),
    CONSTRAINT chk_problems_token_limit_positive
        CHECK (token_limit IS NULL OR token_limit > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='문제 정보, 평가 유형, 토큰 제한, EFFICIENCY 가중치';


-- 4) requirements ------------------------------------------------
CREATE TABLE requirements (
    id             BIGINT        NOT NULL AUTO_INCREMENT           COMMENT '요구사항 고유 ID',
    problem_id     BIGINT        NOT NULL                          COMMENT '문제 ID (problems.id)',
    description    VARCHAR(500)  NOT NULL                          COMMENT 'LLM 평가 프롬프트에 삽입되는 요구사항 설명',
    weight         INT           NOT NULL                          COMMENT '배점 가중치 (문제 내 합계 = 100)',
    display_order  INT           NOT NULL DEFAULT 0                COMMENT '화면 표시 순서',
    PRIMARY KEY (id),
    KEY idx_requirements_problem (problem_id),

    CONSTRAINT fk_requirements_problem
        FOREIGN KEY (problem_id) REFERENCES problems(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_requirements_weight
        CHECK (weight > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='문제별 요구사항 항목 및 배점 가중치';


-- 5) submissions -------------------------------------------------
CREATE TABLE submissions (
    id                  BIGINT      NOT NULL AUTO_INCREMENT        COMMENT '제출 고유 ID',
    user_id             BIGINT      NOT NULL                       COMMENT '사용자 ID (users.id)',
    problem_id          BIGINT      NOT NULL                       COMMENT '문제 ID (problems.id)',
    conversation        JSON        NOT NULL                       COMMENT '전체 대화 이력',
    final_code          TEXT        NOT NULL                       COMMENT '최종 제출 코드 (Java 소스)',
    total_user_tokens   INT         NOT NULL DEFAULT 0             COMMENT '제출 내 사용자 프롬프트 토큰 총합 (EFFICIENCY 평가 기준)',
    completion_tokens   INT         NOT NULL DEFAULT 0             COMMENT 'LLM 출력 토큰 수 (참고/통계용)',
    score               INT         NOT NULL DEFAULT 0             COMMENT '최종 점수 0~100',
    requirements_result JSON        NULL                           COMMENT '요구사항별 달성도 [{id, score, comment}]',
    status              ENUM('GRADING','PASSED','FAILED','ERROR') NOT NULL DEFAULT 'GRADING'
                                                                   COMMENT 'GRADING: 채점 중 / PASSED·FAILED: 채점 완료 / ERROR: LLM 실패',
    submitted_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '제출 일시',
    graded_at           DATETIME    NULL                           COMMENT '채점 완료 일시 (GRADING 동안 NULL)',
    PRIMARY KEY (id),

    KEY idx_submissions_user         (user_id),
    KEY idx_submissions_problem      (problem_id),
    KEY idx_submissions_user_problem (user_id, problem_id),
    KEY idx_submissions_submitted    (submitted_at),

    -- 제출 기록 보존: 사용자/문제 삭제 시 RESTRICT
    CONSTRAINT fk_submissions_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_submissions_problem
        FOREIGN KEY (problem_id) REFERENCES problems(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_submissions_user_tokens
        CHECK (total_user_tokens >= 0),
    CONSTRAINT chk_submissions_completion_tokens
        CHECK (completion_tokens >= 0),
    CONSTRAINT chk_submissions_score
        CHECK (score BETWEEN 0 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='사용자 제출 기록 (대화/최종코드/토큰/점수/채점결과). 모든 제출은 자동 공개.';
