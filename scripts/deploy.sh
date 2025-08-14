#!/bin/bash

set -euo pipefail

EC2_USER=ubuntu
EC2_HOST=3.35.20.93
KEY=~/Downloads/key-farhana.pem
APP_NAME=spring-ecommerce-order
JAR=$(ls build/libs/*.jar | tail -n1)

./gradlew clean bootJar

scp -i "$KEY" "$JAR" $EC2_USER@$EC2_HOST:~/releases/

ssh -i "$KEY" $EC2_USER@$EC2_HOST <<'EOF'
set -e
LATEST=$(ls -t ~/releases/*.jar | head -n1)
ln -sfn "$LATEST" ~/app/app.jar
sudo systemctl restart ecommerce || true

EOF

echo "Deployed: $JAR"
