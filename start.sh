#!/bin/bash
# TimeWeaver 一键启动脚本
# 用法: ./start.sh [dev|build]
set -e

MODE="${1:-dev}"
BACKEND_DIR="$(cd "$(dirname "$0")/backend" && pwd)"
FRONTEND_DIR="$(cd "$(dirname "$0")/frontend" && pwd)"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$BACKEND_DIR/target/timeweaver-backend-1.0.0.jar"

# 颜色
GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  TimeWeaver 启动中...${NC}"
echo -e "${BLUE}========================================${NC}"

# 1. 检查 MySQL
echo -e "${GREEN}[1/4] 检查 MySQL...${NC}"
if mysqladmin ping -uroot -proot 2>/dev/null; then
  echo "  MySQL OK"
else
  echo "  启动 MySQL..."
  mysqld_safe --datadir=/opt/homebrew/var/mysql &>/dev/null &
  sleep 3
fi

# 2. 启动后端
echo -e "${GREEN}[2/4] 启动后端...${NC}"
if [ ! -f "$JAR" ]; then
  echo "  首次运行，编译后端..."
  cd "$BACKEND_DIR" && /opt/homebrew/bin/mvn package -DskipTests -q
fi
AI_API_KEY=sk-placeholder /opt/homebrew/opt/openjdk/bin/java -jar "$JAR" &
BACKEND_PID=$!
echo "  后端 PID: $BACKEND_PID (端口 8080)"
sleep 6

# 3. 启动前端
echo -e "${GREEN}[3/4] 启动前端...${NC}"
cd "$FRONTEND_DIR"
if [ "$MODE" = "build" ]; then
  npx vite build
  echo "  构建完成，打开 dist/index.html 或部署到 Nginx"
else
  npx vite --host &
  FRONTEND_PID=$!
  echo "  前端 PID: $FRONTEND_PID (端口 5173)"
fi

# 4. 启动 AI 追踪
echo -e "${GREEN}[4/4] 启动 AI 追踪...${NC}"
TRACKER_SCRIPT="$PROJECT_DIR/scripts/ai-tracker.sh"
if [ -f "$TRACKER_SCRIPT" ]; then
  # 确保 token 文件存在
  TOKEN_DIR="$HOME/.timeweaver"
  TOKEN_FILE="$TOKEN_DIR/token"
  mkdir -p "$TOKEN_DIR"

  if [ ! -f "$TOKEN_FILE" ] || ! curl -s -H "Authorization: Bearer $(cat "$TOKEN_FILE")" http://localhost:8080/api/ai-sessions?date=2026-06-28 > /dev/null 2>&1; then
    echo "  获取访问令牌..."
    LOGIN_RESP=$(curl -s -X POST http://localhost:8080/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"username":"aider","password":"test123"}')
    NEW_TOKEN=$(echo "$LOGIN_RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))" 2>/dev/null)
    if [ -n "$NEW_TOKEN" ]; then
      echo "$NEW_TOKEN" > "$TOKEN_FILE"
      echo "  Token 已获取"
    fi
  fi

  # 启动 tracker
  bash "$TRACKER_SCRIPT" > /tmp/tracker.log 2>&1 &
  TRACKER_PID=$!
  echo "  AI 追踪 PID: $TRACKER_PID (检测 Claude Code / Cursor / Codex)"
else
  echo -e "  ${YELLOW}未找到 scripts/ai-tracker.sh，跳过 AI 追踪${NC}"
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  TimeWeaver 运行中!${NC}"
echo -e "${BLUE}  前端: http://localhost:5173${NC}"
echo -e "${BLUE}  后端: http://localhost:8080${NC}"
echo -e "${BLUE}  AI 追踪: tail -f /tmp/tracker.log${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "停止: pkill -f timeweaver-backend && pkill -f 'vite' && pkill -f ai-tracker.sh"
