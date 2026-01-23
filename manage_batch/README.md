# Batch Management System

Spring Boot 기반 배치 관리 서비스입니다. 외부 JAR에 포함된 배치 클래스를 스캔하고 등록/실행하며,
실행 이력과 재시도 이력을 관리합니다.

## Requirements
- Java 17
- Maven 3.8+

## Run (Maven)
```bash
mvn -q -DskipTests spring-boot:run
```

## Build
```bash
mvn -q -DskipTests package
```

## Run (Jar)
```bash
java -jar target/batch-management-system-1.0.0.jar
```

## Project Structure
- `src/main/java/com/widetns/batch/BatchManagementApplication.java`: 앱 진입점
- `src/main/java/com/widetns/batch/controller`: 웹 컨트롤러
- `src/main/java/com/widetns/batch/service`: 배치 스캔/등록/실행
- `src/main/java/com/widetns/batch/scheduler`: 스케줄/재시도 처리
- `src/main/java/com/widetns/batch/entity`: JPA 엔티티
- `src/main/java/com/widetns/batch/repository`: JPA 리포지토리
- `src/main/resources/application.yml`: 설정
- `batch-libs/`: 외부 배치 JAR 위치

## Notes
- `batch-libs/` 아래의 JAR에서 `com.widetns.batch.core.BatchJob` 구현체를 스캔합니다.
- 기본 DB는 H2 메모리(`jdbc:h2:mem:batchdb`)로 설정되어 있습니다.
