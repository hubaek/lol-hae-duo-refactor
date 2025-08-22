# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

롤(League of Legends) 유저들을 위한 듀오 매칭 및 전적 분석 서비스입니다. Spring Boot 3.4.0 기반의 백엔드 API 서버로, Riot API를 활용해 게임 데이터를 수집하고 분석합니다.

## 주요 빌드 및 개발 명령어

### 빌드 및 실행
```bash
# 프로젝트 빌드
./gradlew build

# 프로젝트 실행 (개발 환경)
./gradlew bootRun

# 테스트 실행
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "클래스명"

# 전체 클린 빌드
./gradlew clean build
```

### 개발 환경 설정
- Java 17 필수
- MySQL 8.0 (로컬 개발용)
- Redis (큐 시스템용)
- application.yml에서 DB 및 Redis 연결 정보 설정 필요

## 아키텍처 및 주요 설계 패턴

### 도메인 기반 패키지 구조
프로젝트는 도메인별로 패키지를 구분하여 구성되어 있습니다:
- `domain.account`: 계정 관리 (Riot 계정 연동, 게임 데이터)
- `domain.duo`: 듀오 매칭 시스템
- `domain.member`: 멤버 관리 및 인증
- `client`: Riot API 클라이언트 및 외부 API 통신
- `common`: 공통 설정, 예외 처리, 유틸리티

### 비동기 처리 아키텍처
1. **Spring Event 기반 비동기 처리**: 계정 연동 시 게임 데이터 수집을 비동기로 처리
2. **Redis 기반 API 큐 시스템**: Riot API 호출을 큐에서 관리하여 Rate Limit 문제 해결
3. **CompletableFuture**: 대량의 매치 데이터를 병렬로 처리

### Rate Limiting 전략
- `RateLimiterManager`: Bucket4j를 사용한 내부 Rate Limit 관리
- `RedisRiotApiQueueService`: Redis 기반 비동기 API 요청 큐 시스템
- Spring Retry를 활용한 실패 요청 재시도 로직

### 주요 서비스 클래스
- `RiotClientService`: Riot API 통신 및 게임 데이터 수집의 핵심 로직
- `AccountGameDataService`: 계정별 게임 데이터 관리
- `DuoService`: 듀오 매칭 비즈니스 로직

### 데이터베이스 설계
- JPA/Hibernate 사용, QueryDSL로 복잡한 쿼리 처리
- `Timestamped` 엔티티를 상속받아 생성/수정 시간 자동 관리
- `@EnableJpaAuditing`으로 감사 기능 활성화

### 인증 및 보안
- JWT 기반 인증 시스템
- `@Auth` 커스텀 어노테이션으로 인증이 필요한 엔드포인트 표시
- `AuthArgumentResolver`로 토큰에서 사용자 정보 추출

### 스케줄링
- `@EnableScheduling`으로 스케줄링 기능 활성화
- `DataDragonScheduler`: 게임 데이터 주기적 업데이트
- `AccountGameDataScheduler`: 계정별 게임 데이터 주기적 갱신

### 모니터링
- Spring Boot Actuator + Prometheus + Grafana 스택
- 성능 메트릭 및 API 호출 모니터링

## 주요 기술 스택 및 의존성

### 핵심 라이브러리
- **Spring Boot 3.4.0**: 메인 프레임워크
- **Spring Data JPA**: 데이터 액세스
- **QueryDSL**: 동적 쿼리 생성
- **Spring Retry**: 실패 요청 재시도
- **Bucket4j**: Rate Limiting
- **Redisson**: Redis 클라이언트
- **JWT (jjwt)**: 인증 토큰

### 외부 API
- **Riot Games API**: 게임 데이터 수집
- **Data Dragon API**: 정적 게임 데이터 (챔피언, 아이템 등)

## 개발 시 주의사항

### Riot API 관련
- Rate Limit 준수 필수: 초당 20회, 2분당 100회 제한
- `RateLimiterManager`를 통한 요청 전 Rate Limit 체크
- 실패 시 Spring Retry를 통한 자동 재시도

### 비동기 처리
- `AccountGameDataEvent` 발생 시 백그라운드에서 게임 데이터 수집
- `CompletableFuture`를 사용한 병렬 처리 시 스레드 풀 크기 고려

### 데이터베이스
- 개발환경에서는 `ddl-auto: update` 사용
- 운영환경에서는 `validate` 또는 `none` 권장
- QueryDSL Q클래스는 빌드 시 자동 생성됨

### 설정 파일
- `application.yml`: 기본 설정
- `application-prod.yml`: 운영 환경 설정
- Redis 프로필이 기본으로 활성화됨