# HowProm

AI 프롬프트 작성 훈련 플랫폼 - Spring Boot + Thymeleaf

## 요구사항

- Java 17 이상 (Java 21 권장)
- (선택) Maven 3.9+ — 없으면 IDE 또는 Maven Wrapper 사용

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

## 프로젝트 구조

```
howprom/
├── pom.xml
└── src/main/
    ├── java/com/howprom/
    │   ├── HowPromApplication.java       # 메인 애플리케이션
    │   └── controller/
    │       └── MainController.java        # "/" 와 "/main" 라우팅
    └── resources/
        ├── application.properties
        └── templates/
            ├── common/
            │   └── header.html            # 공통 헤더 fragment + styles
            └── main/
                └── main.html              # 대시보드 페이지
```

## 실행 방법

### 1. IntelliJ IDEA / VS Code 등 IDE에서 실행

`HowPromApplication.java` 파일을 열고 main 메서드를 실행하세요.

### 2. Maven이 설치된 경우

```bash
mvn spring-boot:run
```

### 3. 빌드 후 실행 (배포용)

```bash
mvn clean package
java -jar target/howprom-0.0.1-SNAPSHOT.jar
```

## 접속

브라우저에서 다음 주소로 접속합니다:

- http://localhost:8080/
- http://localhost:8080/main

## 주요 설정

- 포트: `8080` (변경: `application.properties`의 `server.port`)
- Thymeleaf 캐시: 개발 편의를 위해 꺼져 있음 (`spring.thymeleaf.cache=false`)
- DevTools: 추가되어 있어 클래스 변경 시 자동 재시작

## 페이지 추가하기

새 페이지를 만들려면:

1. `templates/` 아래에 새 HTML 파일 생성 (예: `templates/problems/problems.html`)
2. 헤더 import:
   ```html
   <th:block th:replace="~{common/header :: styles}"></th:block>
   <th:block th:replace="~{common/header :: header('problems')}"></th:block>
   ```
3. Controller에 매핑 추가:
   ```java
   @GetMapping("/problems")
   public String problems(Model model) {
       return "problems/problems";
   }
   ```

header fragment에 전달하는 active 값은 `dashboard`, `problems`, `community`, `admin` 중 하나입니다.

## 다음 단계

- DB 연동: `spring-boot-starter-data-jpa` + H2/MySQL 의존성 추가
- 로그인: `spring-boot-starter-security` 추가
- API: REST 컨트롤러로 `/api/problems`, `/api/submissions` 등 추가
