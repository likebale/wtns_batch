# WTNS Batch

Java/Spring Boot 기반의 멀티모듈 배치 관리 프로젝트입니다.

## 프로젝트 개요
- 배치 라이브러리(JAR) 기반으로 배치를 스캔/등록/실행
- 배치 실행 이력 및 실패 재시도 이력 관리
- 웹 관리 화면(Thymeleaf)에서 수동 실행 및 상태 조회

## 모듈 구성
- `batch_core_lib` (`batch-job-core`): 배치 구현체가 따르는 공통 인터페이스/코어 라이브러리
- `sample_batch_lib` (`sample-batch-lib`): 샘플 배치 라이브러리, 빌드 시 관리 앱의 `batch-libs`로 복사
- `manage_batch` (`batch-management-system`): 배치 관리 웹 애플리케이션

## 기술 스택
- Java 21
- Spring Boot 3.2.0
- Spring Web, Spring Data JPA, Thymeleaf
- H2 (기본), MySQL Connector 포함
- Maven (멀티모듈)

## 사전 요구사항
- JDK 21
- Maven 3.8+

## 빠른 시작
### 1) 전체 빌드
루트 경로(`wtns_batch`)에서 실행:

```bash
mvn -DskipTests clean package
```

빌드 시 `sample_batch_lib`의 JAR가 `manage_batch/batch-libs`로 복사됩니다.

### 2) 관리 앱 실행
루트에서 모듈 지정 실행:

```bash
mvn -pl manage_batch -DskipTests spring-boot:run
```

또는 JAR 실행:

```bash
java -jar manage_batch/target/batch-management-system-1.0.0.jar
```

### 3) 접속
- 관리 화면: `http://localhost:8080`

## 기본 설정
`manage_batch/src/main/resources/application.yml` 기준:
- 서버 포트: `8080`
- 기본 DB: `jdbc:h2:mem:batchdb`
- 배치 라이브러리 경로: `manage_batch/batch-libs`
- 시작 시 배치 스캔: `batch.scan-on-startup: true`

## 주요 디렉터리
- `pom.xml`: 루트 멀티모듈 빌드 설정
- `batch_core_lib/`: 배치 코어 라이브러리
- `sample_batch_lib/`: 샘플 배치 구현
- `manage_batch/`: 배치 관리 웹 애플리케이션

## 참고
관리 앱 상세 문서는 `manage_batch/README.md`를 참고하세요.
