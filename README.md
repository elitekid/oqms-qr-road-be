# 실행 환경별 YAML 설정 방법

## 1. 공통 설정 (application.yaml)
- 전체 환경에서 공통으로 사용하는 설정을 `src/main/resources/application.yaml`에 작성합니다.

## 2. 로컬 환경 설정 (application-local.yaml)
- 로컬에서 실행할 때는 반드시 `spring.datasource`의 모든 항목(`url`, `username`, `password`)을 포함해야 합니다.

### 예시: application-local.yaml
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_db
    username: your_username
    password: your_password
```
- 필요에 따라 JPA, Hibernate 등 추가 옵션도 설정 가능합니다.

---

# Local 환경에서 구동 방법

1. `application-local.yaml` 파일을 위 예시처럼 작성합니다.
2. VM 옵션 또는 환경변수로 `spring.profiles.active=local`을 지정하여 실행합니다.
    - 예시:
      ```
      --spring.profiles.active=local
      ```
    - 또는 IDE에서 "Active Profile"에 `local` 입력

3. 정상적으로 datasource가 설정되어 있으면, Spring Boot가 자동으로 DB 연결을 구성합니다.

---

이 내용을 참고하여 환경별 설정 및 로컬 구동을 진행해 주세요.
