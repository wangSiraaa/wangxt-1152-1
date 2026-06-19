#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ -f .env ]; then
  set -a
  . ./.env
  set +a
fi

PORT=${WEB_PORT:-20452}

echo "============================================"
echo "  林业有害生物诱捕监测系统 启动脚本"
echo "============================================"
echo ""
echo "[1/3] 检查 Maven..."
if ! command -v mvn >/dev/null 2>&1; then
  echo "错误: 未找到 Maven，请先安装 Maven"
  exit 1
fi
echo "✅ Maven 已安装"

echo ""
echo "[2/3] 编译项目..."
mvn clean package -DskipTests -q
echo "✅ 编译完成"

echo ""
echo "[3/3] 启动服务 (端口: ${PORT})..."
echo ""

JAR_FILE="target/forest-pest-monitor.jar"
if [ ! -f "$JAR_FILE" ]; then
  JAR_FILE=$(ls target/*.jar 2>/dev/null | head -1)
fi

if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
  echo "错误: 找不到编译后的jar文件"
  exit 1
fi

java -jar "$JAR_FILE" "$PORT"
