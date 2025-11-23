#!/bin/bash

cd "$(dirname "$0")"

DEFAULT_PORT=9001

APP_JAR="./app/build/libs/app.jar"

if [ ! -f "$APP_JAR" ]; then
  echo "[ERROR] $APP_JAR 파일을 찾을 수 없습니다."
  echo "app jar를 먼저 빌드한 뒤 해당 위치로 복사해 주세요."
  read -n 1 -s -r -p "계속하려면 아무 키나 누르세요..."
  exit 1
fi

echo "[INFO] Spring Boot 서버를 실행합니다..."
echo "[INFO] Jar: $APP_JAR"

java -jar "$APP_JAR"

read -n 1 -s -r -p "서버가 종료되었습니다. 창을 닫으려면 아무 키나 누르세요..."
