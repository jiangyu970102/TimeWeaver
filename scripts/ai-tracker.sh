#!/bin/bash
# TimeWeaver AI Tracker - 自动检测 AI 工具使用并记录到后端 API
# 使用方式: ./scripts/ai-tracker.sh [interval_seconds]
# 默认每 30 秒检测一次
#
# 兼容性: macOS (bash 3.2) + Linux

INTERVAL=${1:-30}
API_BASE="${API_BASE:-http://localhost:8080/api}"
TOKEN="${API_TOKEN:-}"

# 状态文件目录（避免使用 bash 4+ 关联数组）
STATE_DIR="${TMPDIR:-/tmp}/timeweaver-tracker"
mkdir -p "$STATE_DIR"

# 如果没有 token，尝试从本地存储读取
if [ -z "$TOKEN" ]; then
  TOKEN_DIR="${HOME}/.timeweaver"
  TOKEN_FILE="${TOKEN_DIR}/token"
  if [ -f "$TOKEN_FILE" ]; then
    TOKEN=$(cat "$TOKEN_FILE")
  fi
fi

if [ -z "$TOKEN" ]; then
  echo "错误: 未设置 API_TOKEN 环境变量，也无法从 ~/.timeweaver/token 读取"
  echo "请先登录: export API_TOKEN=your_jwt_token"
  exit 1
fi

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

# ---------- 状态管理（兼容 bash 3.2） ----------
state_read() {
  local key="$1"
  local file="${STATE_DIR}/${key}"
  if [ -f "$file" ]; then
    cat "$file"
  else
    echo ""
  fi
}

state_write() {
  local key="$1"
  local val="$2"
  echo "$val" > "${STATE_DIR}/${key}"
}

state_del() {
  local key="$1"
  rm -f "${STATE_DIR}/${key}"
}

# ---------- 本地时间（匹配应用时区 Asia/Shanghai） ----------
local_now_iso() {
  date '+%Y-%m-%dT%H:%M:%S'
}

local_timestamp() {
  date '+%s'
}

# ---------- 检测活跃 AI 工具 ----------
detect_active_ai_tools() {
  local tools=""

  if [ "$(uname)" != "Darwin" ]; then
    # Linux: check active window title
    local active_window
    active_window=$(xdotool getactivewindow getwindowname 2>/dev/null)
    if echo "$active_window" | grep -qiE "claude"; then tools="$tools claude-code"; fi
    if echo "$active_window" | grep -qiE "cursor"; then tools="$tools cursor"; fi
    if echo "$active_window" | grep -qiE "codex"; then tools="$tools codex"; fi
    if echo "$active_window" | grep -qiE "chatgpt"; then tools="$tools chatgpt"; fi
    if echo "$active_window" | grep -qiE "kimi"; then tools="$tools kimi"; fi
    echo "$tools"
    return
  fi

  # macOS: get frontmost app
  local app_name
  app_name=$(osascript -e 'tell application "System Events" to get name of first application process whose frontmost is true' 2>/dev/null)

  # 1. Direct app name match
  case "$app_name" in
    "Cursor")           echo "cursor"; return ;;
    "Codex")            echo "codex"; return ;;
    "Claude"|"Claude Code") echo "claude-code"; return ;;
    "ChatGPT"|"ChatGPT Desktop") echo "chatgpt"; return ;;
    "Kimi"|"Kimi AI")   echo "kimi"; return ;;
  esac

  # 2. Terminal/IDE: detect ALL running AI tools (one per line)
  case "$app_name" in
    "Terminal"|"iTerm2"|"Warp"|"tmux"|"Visual Studio Code"|"VS Code"|"Code")
      local proc_basenames
      proc_basenames=$(ps -eo comm= 2>/dev/null | tr -d '()' | grep -oE '[^/]+$' | sort -fu)
      if echo "$proc_basenames" | grep -qi '^claude$'; then tools="$tools claude-code"; fi
      if echo "$proc_basenames" | grep -qi '^cursor$'; then tools="$tools cursor"; fi
      if echo "$proc_basenames" | grep -qi '^codex$'; then tools="$tools codex"; fi
      if echo "$proc_basenames" | grep -qi '^kimi$'; then tools="$tools kimi"; fi
      ;;
  esac

  echo "$tools"
}

# ---------- 默认模型映射 ----------
tool_default_model() {
  case "$1" in
    "claude-code") echo "deepseek-v4-flash" ;;
    "cursor")      echo "gpt-4o" ;;
    "codex")       echo "gpt-4o" ;;
    "chatgpt")     echo "gpt-4o" ;;
    "kimi")        echo "kimi-k2" ;;
    *)             echo "other" ;;
  esac
}

# ---------- 记录会话 ----------
record_session() {
  local tool="$1"
  [ -z "$tool" ] && return

  local model
  model=$(tool_default_model "$tool")
  local current_ts
  current_ts=$(local_timestamp)
  local state_key="session_${tool}"

  # 读取状态
  local last_start
  last_start=$(state_read "${state_key}_start")
  local last_id
  last_id=$(state_read "${state_key}_id")

  if [ -n "$last_start" ] && [ -n "$last_id" ]; then
    local elapsed=$((current_ts - last_start))
    if [ "$elapsed" -gt 300 ]; then
      # 超过 5 分钟，结束旧会话
      local end_time
      end_time=$(local_now_iso)

      curl -s -X PUT "${API_BASE}/ai-sessions/${last_id}" \
        -H "Authorization: Bearer ${TOKEN}" \
        -H "Content-Type: application/json" \
        -d "{\"endTime\":\"${end_time}\",\"durationSec\":${elapsed}}" > /dev/null
      log "结束会话: ${tool}/${model} (${elapsed}s)"
      state_del "${state_key}_start"
      state_del "${state_key}_id"
    else
      # 仍在活跃区间内
      return
    fi
  fi

  # 创建新会话
  local start_time
  start_time=$(local_now_iso)
  local response
  response=$(curl -s -X POST "${API_BASE}/ai-sessions" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{\"toolName\":\"${tool}\",\"modelName\":\"${model}\",\"startTime\":\"${start_time}\",\"source\":\"detect\"}")

  local new_id
  new_id=$(echo "$response" | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('id',''))" 2>/dev/null)

  if [ -n "$new_id" ]; then
    state_write "${state_key}_start" "$current_ts"
    state_write "${state_key}_id" "$new_id"
    log "新会话: ${tool}/${model} (ID: ${new_id})"
  fi
}

# ---------- 主循环 ----------
log "TimeWeaver AI Tracker 启动 (间隔: ${INTERVAL}s)"
log "API: ${API_BASE}"
log "状态目录: ${STATE_DIR}"

while true; do
  tools=$(detect_active_ai_tools)
  for tool in $tools; do
    record_session "$tool"
  done
  sleep "$INTERVAL"
done
