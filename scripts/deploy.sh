#!/usr/bin/env bash
set -euo pipefail

EC2_USER=ubuntu
EC2_HOST=3.35.20.93
KEY="$HOME/Downloads/key-farhana.pem"

APP_NAME=spring-ecommerce-order

echo "[1/3] Build jar…"
./gradlew clean bootJar

echo "[2/3] Pick latest jar…"
JAR=$(ls -t build/libs/*.jar | head -n1)
echo "JAR: $JAR"

echo "[3/3] Upload to EC2 ~/releases…"
scp -i "$KEY" "$JAR" "$EC2_USER@$EC2_HOST:~/releases/"

ssh -i "$KEY" "$EC2_USER@$EC2_HOST" <<'EOF'
set -e
RELEASE_DIR="$HOME/releases"
APP_DIR="$HOME/app"
APP_JAR_LINK="$APP_DIR/app.jar"

LATEST_JAR=$(ls -t "$RELEASE_DIR"/*.jar | head -n1)
mkdir -p "$APP_DIR"
ln -sfn "$LATEST_JAR" "$APP_JAR_LINK"

if systemctl list-unit-files | grep -q '^ecommerce.service'; then
  sudo systemctl restart ecommerce
else
  pkill -f "$APP_JAR_LINK" || true
  nohup java -jar "$APP_JAR_LINK" > "$APP_DIR/app.out" 2>&1 &
  sleep 3
fi

curl -sS http://localhost:8080/actuator/health || true
EOF

echo "Deployed: $JAR"
