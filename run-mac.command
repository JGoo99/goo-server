#!/bin/bash

# 스크립트 자체 실행 권한 보정
chmod +x "$0"

# 스크립트 기준 절대 경로 계산
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

APP_JAR="$BASE_DIR/server/app.jar"
CONFIG_FILE="$BASE_DIR/config/application.yml"
LOG_DIR="$BASE_DIR/logs"

echo "[INFO] Script base directory: $BASE_DIR"
echo "[INFO] App jar: $APP_JAR"
echo "[INFO] Config file: $CONFIG_FILE"
echo "[INFO] Log dir: $LOG_DIR"

# Preflight 체크 (Java 설치 여부, JAR 존재 여부, logs 디렉터리 쓰기 가능 여부, config 존재 여부 경고)

# Java 명령어 확인
if ! command -v java >/dev/null 2>&1; then
  echo "[ERROR] 'java' 명령을 찾을 수 없습니다."
  echo " - JDK 또는 JRE가 설치되어 있는지 확인해 주세요."
  echo " - 설치 후 다시 run-mac.command 를 실행해 주세요."
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi

# app.jar 존재 및 읽기 가능 여부
if [ ! -f "$APP_JAR" ]; then
  echo "[ERROR] app.jar 파일을 찾을 수 없습니다."
  echo " - 위치: $APP_JAR"
  echo " - server/app.jar 가 존재하는지 확인해 주세요."
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi

if [ ! -r "$APP_JAR" ]; then
  echo "[ERROR] app.jar 파일을 읽을 수 없습니다 (권한 문제)."
  echo " - 위치: $APP_JAR"
  echo " - 읽기 권한(r)을 부여해 주세요."
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi

# logs 디렉터리 쓰기 권한 확인
mkdir -p "$LOG_DIR" 2>/dev/null
if [ ! -d "$LOG_DIR" ]; then
  echo "[ERROR] logs 디렉터리를 생성할 수 없습니다."
  echo " - 위치: $LOG_DIR"
  echo " - 디스크 권한 또는 읽기전용 위치인지 확인해 주세요."
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi

TEST_LOG_FILE="$LOG_DIR/.write-test-$$.tmp"
if ! echo "test" > "$TEST_LOG_FILE" 2>/dev/null; then
  echo "[ERROR] logs 디렉터리에 파일을 쓸 수 없습니다."
  echo " - 위치: $LOG_DIR"
  echo " - 쓰기 권한(w)을 부여해 주세요."
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi
rm -f "$TEST_LOG_FILE" 2>/dev/null

# config 파일 존재 여부 (없어도 실행은 가능, 단 경고)
if [ ! -f "$CONFIG_FILE" ]; then
  echo "[WARN] config/application.yml 파일을 찾지 못했습니다."
  echo " - 기본 설정(server.port=9001 등)을 사용합니다."
else
  echo "[INFO] config/application.yml 파일을 발견했습니다."
fi

find_existing_process() {
  local pid=""

  # pgrep 이 있으면 우선 사용
  if command -v pgrep >/dev/null 2>&1; then
    # java 프로세스 중에서 APP_JAR 경로를 커맨드라인에 포함하는 프로세스 찾기
    pid=$(pgrep -f "java .*${APP_JAR}")
  else
    # pgrep 이 없으면 ps + grep 조합
    # grep 자기 자신은 제외
    pid=$(ps aux | grep "java" | grep "$APP_JAR" | grep -v "grep" | awk '{print $2}' | head -n 1)
  fi

  echo "$pid"
}

EXISTING_PID="$(find_existing_process)"

if [ -n "$EXISTING_PID" ]; then
  echo "[ERROR] 이미 이 애플리케이션이 실행 중입니다."
  echo " - PID: $EXISTING_PID"
  echo " - 중복 실행을 방지하기 위해 새 인스턴스를 시작하지 않습니다."
  echo ""
  echo "프로세스를 종료하려면 예를 들어 다음과 같이 실행할 수 있습니다:"
  echo "  kill $EXISTING_PID"
  echo "또는 활동 모니터에서 해당 Java 프로세스를 종료해 주세요."
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi

# 포트 결정 로직 (config → 기본값)
BASE_PORT=9001
MAX_PORT=9999

if [ -f "$CONFIG_FILE" ]; then
  CFG_PORT=$(grep -E "^[[:space:]]*server\.port" "$CONFIG_FILE" | head -n 1 | awk -F':' '{gsub(/ /,"",$2); print $2}')
  if [ -n "$CFG_PORT" ]; then
    BASE_PORT="$CFG_PORT"
  fi
fi

echo "[INFO] 기본 시작 포트: $BASE_PORT (최대: $MAX_PORT)"

# 빈 포트 탐색 함수
find_free_port() {
  local port="$BASE_PORT"

  # lsof 가 없으면 포트 탐색을 할 수 없으므로 BASE_PORT 그대로 사용
  if ! command -v lsof >/dev/null 2>&1; then
    echo "$port"
    return
  fi

  while [ "$port" -le "$MAX_PORT" ]; do
    if ! lsof -i TCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
      echo "$port"
      return
    fi
    port=$((port + 1))
  done

  echo ""
}

FREE_PORT="$(find_free_port)"

if [ -z "$FREE_PORT" ]; then
  echo "[ERROR] ${BASE_PORT}~${MAX_PORT} 범위에서 사용 가능한 포트를 찾지 못했습니다."
  echo " - 다른 프로그램이 포트를 이미 점유하고 있을 수 있습니다."
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi

echo "[INFO] 사용 가능한 포트 발견: $FREE_PORT"

# 서버 실행
echo "[INFO] Spring Boot 서버 실행 시작..."
echo "[INFO] java -jar \"$APP_JAR\" --server.port=$FREE_PORT"
echo ""

java -jar "$APP_JAR" --server.port="$FREE_PORT"

echo ""
read -n 1 -s -r -p "서버가 종료되었습니다. 창을 닫으려면 아무 키나 누르세요..."
