#!/bin/bash

chmod +x "$0"

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

APP_JAR="$BASE_DIR/server/app.jar"
CONFIG_FILE="$BASE_DIR/config/application.yml"

echo "[INFO] Script base directory: $BASE_DIR"
echo "[INFO] Checking jar: $APP_JAR"

if [ ! -f "$APP_JAR" ]; then
  echo "[ERROR] app.jar 파일을 찾을 수 없습니다."
  echo "[ERROR] 위치: $APP_JAR"
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi

PORT=""
if [ -f "$CONFIG_FILE" ]; then
  PORT=$(grep -E "^[[:space:]]*server\.port" "$CONFIG_FILE" | head -n 1 | awk -F':' '{gsub(/ /,"",$2); print $2}')
fi

if [ -z "$PORT" ]; then
  PORT=9001
fi

echo "[INFO] 사용 포트: $PORT"

if command -v lsof >/dev/null 2>&1; then
  if lsof -i TCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "[ERROR] 포트 $PORT 가 이미 사용 중입니다."
    read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
    exit 1
  fi
fi

echo "[INFO] Spring Boot 서버 실행 시작..."
echo ""

java -jar "$APP_JAR"

echo ""
read -n 1 -s -r -p "서버가 종료되었습니다. 창을 닫으려면 아무 키나 누르세요..."
