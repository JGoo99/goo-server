#!/bin/bash

# 스크립트 자체에 실행 권한 부여
chmod +x "$0"

# 스크립트 기준 절대 경로
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

APP_JAR="$BASE_DIR/server/app.jar"
CONFIG_FILE="$BASE_DIR/config/application.yml"

echo "[INFO] Script base directory: $BASE_DIR"
echo "[INFO] Checking jar: $APP_JAR"

# jar 존재 여부 확인
if [ ! -f "$APP_JAR" ]; then
  echo "[ERROR] app.jar 파일을 찾을 수 없습니다."
  echo "[ERROR] 위치: $APP_JAR"
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi

# 기본 포트 설정 (config에서 읽히지 않을 경우)
BASE_PORT=9001

# config/application.yml 에서 server.port 읽기
if [ -f "$CONFIG_FILE" ]; then
  CFG_PORT=$(grep -E "^[[:space:]]*server\.port" "$CONFIG_FILE" | head -n 1 | awk -F':' '{gsub(/ /,"",$2); print $2}')
  if [ -n "$CFG_PORT" ]; then
    BASE_PORT="$CFG_PORT"
  fi
fi

MAX_PORT=9999

echo "[INFO] 설정 기준 시작 포트: $BASE_PORT"

# 빈 포트 탐색 함수
find_free_port() {
  port=$BASE_PORT
  while [ "$port" -le "$MAX_PORT" ]; do
    if ! lsof -i TCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
      echo "$port"
      return
    fi
    port=$((port+1))
  done
  echo ""
}

# 실제 사용 포트 찾기
FREE_PORT="$(find_free_port)"

if [ -z "$FREE_PORT" ]; then
  echo "[ERROR] ${BASE_PORT}~${MAX_PORT} 범위에서 사용 가능한 포트를 찾지 못했습니다."
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
