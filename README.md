# Local GooServer

더블클릭 한 번으로 실행되는 스프링부트 경량 배포 패키지

---

## 📌 개요

이 프로젝트는 **로컬 환경에서 비용 없이 빠르고 안정적으로 배포하는 실험**으로 시작되었다.

사용자는 단순히 `run-mac.command` 를 더블클릭하는 것만으로 서버를 실행할 수 있다.

서버 실행 전 환경 검증, 포트 자동 탐색, 프로세스 중복 실행 방지, 요청 로그 기록,

프로세스 상태 API, graceful shutdown 등 운영에 필요한 기본 요소들을 직접 구현했다.

---

## 📁 폴더 구조

```
release/
└── goo-server/
    ├── config/
    │   └── application.yml
    ├── logs/
    ├── run-mac.command
    └── server/
        └── app.jar
```

---

## 🚀 실행 방법 (macOS 기준)

1. 패키지 전체를 한 폴더로 다운로드
2. `run-mac.command` 더블클릭
3. 자동 환경검증 및 서버 실행
4. 브라우저에서:

    ```
    http://localhost:9001
    ```

5. 포트가 겹칠 경우 자동으로 9002~9999 범위에서 빈 포트를 찾아 실행

---

## ✔ 주요 기능 요약

### 1) 실행 전 환경 검사 (Preflight)

- Java 설치 여부
- app.jar 존재 및 권한
- logs 디렉토리 쓰기 가능 여부
- config 파일 존재 여부
- 이미 실행 중인 프로세스 여부

### 2) 포트 자동 탐색

기본 포트 사용 불가 시, 9001 ~ 9999 범위에서 빈 포트 자동 선택.

### 3) 프로세스 중복 실행 방지

이미 동일 app.jar이 실행 중이면 재실행을 막고 PID 를 안내.

### 4) 요청 로그 기록

매 요청마다 파일 로그 (`logs/app-YYYYMMDD.log`) 기록.

`/logs/recent?lines=200` 로 최근 로그 조회 가능.

### 5) 프로세스 상태 API

```
GET /process/status

```

반환 예:

```json
{
  "startTime": "...",
  "uptimeSec": 1233,
  "totalRequests": 82,
  "totalErrors": 1,
  "avgDurationMs": 15.2,
  "lastError": { ... }
}
```

### 6) Graceful Shutdown

서버 종료 시:

- 프로세스 요약을 `shutdown-history.log`에 기록

---

## 🔧 개발/학습 포인트

- 스프링부트 서버의 부팅 → 실행 → 종료까지 라이프사이클 이해
- 운영에서 중요한 관찰성(observability) 요소 직접 구현
- 로그 기록 + 상태 정보 수집 구조 설계
- 서버 실행 경험(UX) 자체를 프로그래밍으로 풀어내는 실험