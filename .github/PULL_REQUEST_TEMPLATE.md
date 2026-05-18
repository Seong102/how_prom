## 📌 개요
- 
- 

## 🔗 관련 이슈
- Issue: #

---

## ✅ 구현 검증 항목

### 1. Repository (Spring Data JPA)
- [ ] Spring Data JPA 인터페이스 또는 `@Query`를 사용했는가?
- [ ] N+1 문제가 발생하지 않는가? (fetch join 또는 `@EntityGraph` 적용)
- [ ] 트랜잭션이 필요한 쿼리에 `@Transactional`이 적용되었는가?
- [ ] 불필요한 컬럼까지 SELECT하지 않는가? (Projection 또는 DTO 조회 활용)

### 2. Service Layer
- [ ] 비즈니스 로직 시작 전 입력값 검증(Validation)을 수행했는가?
- [ ] 트랜잭션 범위가 적절하게 설정되었는가? (`@Transactional`)
- [ ] 예외는 의미 있는 메시지와 함께 던지는가?
- [ ] 다른 Service를 직접 호출하지 않고 Repository를 통해 처리했는가?

### 3. Controller
- [ ] `@RestController` 또는 `@Controller`로 올바르게 선언했는가?
- [ ] 비즈니스 로직을 직접 구현하지 않고 Service로 위임했는가?
- [ ] Spring Security 권한 체크가 적용되었는가? (`@PreAuthorize`)
- [ ] 요청/응답 데이터가 DTO 형태로 처리되는가? (Entity 직접 노출 금지)

### 4. LLM 연동 (해당 기능만)
- [ ] WebClient 호출에 타임아웃이 설정되어 있는가? (기본 30초)
- [ ] LLM 응답 JSON 파싱 실패에 대한 `try-catch` 처리가 있는가?
- [ ] 재시도 로직이 구현되어 있는가? (1회 재시도 후 ERROR 상태 저장)
- [ ] 토큰 수 계산 로직이 올바른가? (`role=user` 메시지만 합산)

### 5. View (Thymeleaf)
- [ ] XSS 방지를 위해 `th:text`를 사용했는가? (`th:utext` 사용 최소화)
- [ ] 뷰 파일에 비즈니스 로직이 없는가? (Thymeleaf 표현식만 사용)
- [ ] 공통 레이아웃(`layout:decorate`)을 올바르게 사용했는가?

---

## 📸 스크린샷 (선택)

## 💬 리뷰어에게
