# TimeWeaver — 智能时间投资分析平台

> 像记账一样记录时间，AI 分析你的效率模式并给出可执行的改进建议。

TimeWeaver 是一个面向程序员和知识工作者的全栈时间追踪与 AI 效率分析平台。它不仅仅是倒计时工具，而是形成 **"记录 → 分析 → 建议 → 改进"** 闭环的智能时间投资系统。

---

## 功能特性

### 时间记录
- **一键计时** — 开始/停止记录当前活动，支持手动回溯补录
- **分类管理** — 系统预设 + 用户自定义分类，支持颜色和图标
- **时间轴** — 按日期展示全天活动记录，彩色分类标签一目了然

### 番茄钟
- 专注 25 分钟 / 休息 5 分钟的循环计时
- 完成自动记录到时间轴
- 今日完成数、连续天数统计

### 数据分析仪表盘
- **日历热力图** — 每日各时段活动分布
- **环形图** — 各分类时间占比
- **折线图** — 日/周高效时长趋势
- **桑基图** — 时间流向分析
- **雷达图** — 多维度效率评估
- **柱状图** — 本周 vs 上周分类耗时对比

### 目标管理
- 设定日/周/月目标
- 实时进度追踪
- WebSocket 实时推送提醒

### AI 智能周报（核心差异化）
- 每周自动生成（Spring Schedule 定时触发）
- 分析本周效率模式与趋势
- 发现用户效率洞察（如"周三下午效率最高"）
- 可执行的行动建议
- WebSocket 实时推送生成完成通知

### AI 会话追踪（新增）
- **自动检测** — 本地脚本实时检测 Claude Code / Cursor / Codex / Kimi 等 AI 工具活跃状态
- **时长记录** — 自动记录每次 AI 会话的开始、结束及持续时间
- **可视化分析** — 工具分布、模型分布、每日趋势图表
- **手动管理** — 支持手动新增/编辑/删除会话记录，补充自动检测的遗漏

### 实时通知
- 目标达成/偏离提醒
- 周报生成完成推送
- WebSocket 长连接，自动重连

---

## 技术栈

### 后端

| 技术 | 用途 |
|------|------|
| **Spring Boot 3.4.1** | 项目骨架，IoC 容器，自动配置 |
| **Spring Security + JWT** | 用户认证与授权 |
| **Spring AI 1.0.0-M6** | 调用 LLM 生成结构化 AI 周报 |
| **Spring WebSocket** | 实时推送通知 |
| **MyBatis-Plus 3.5.9** | ORM，数据访问层 |
| **MySQL 8.0** | 数据持久化 |
| **Redis** | 缓存、日活统计、限流 |
| **Spring Schedule** | 定时任务（周报自动生成） |
| **Lombok + Validation** | 减少样板代码 + 参数校验 |

### 前端

| 技术 | 用途 |
|------|------|
| **Vue 3 + Vite 6** | 前端框架 + 构建工具 |
| **Element Plus 2.8** | UI 组件库 |
| **ECharts 5.5** | 数据可视化（6 种图表） |
| **Pinia** | 状态管理 |
| **Vue Router** | 前端路由 |
| **Axios** | HTTP 请求 |
| **Day.js** | 日期处理 |

### 辅助脚本

| 脚本 | 用途 |
|------|------|
| **`scripts/ai-tracker.sh`** | 本地守护进程，实时检测 AI 工具进程并自动记录会话 |
| **`start.sh`** | 一键启动后端 + 前端 + AI 追踪 |

### 部署

| 技术 | 用途 |
|------|------|
| **Docker + Docker Compose** | 容器化部署 |
| **Nginx** | 反向代理 + SPA 路由 |

---

## 系统架构

```
┌─────────────────────────────────────────────────┐
│                  Frontend (Vue 3)                  │
│  Element Plus UI  │  ECharts 图表  │  Pinia 状态  │
└──────────────────┬──────────────────────────────┘
                   │ HTTP / WebSocket
┌──────────────────▼──────────────────────────────┐
│                 Nginx (反向代理)                    │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────┐
│           Controller 层 (REST API)                │
│  Auth  │  Record  │  Stats  │  Goal  │  Report   │
├─────────────────────────────────────────────────┤
│              Service 层 (业务逻辑)                  │
│  TimeRecord  │  Analysis  │  Goal  │  Report     │
├─────────────────────────────────────────────────┤
│              Repository 层 (数据访问)                │
│              MyBatis-Plus BaseMapper                │
├─────────────────────────────────────────────────┤
│     MySQL  │  Redis  │  LLM API (外部)            │
└─────────────────────────────────────────────────┘
```

---

## 快速开始

### 环境要求

- **JDK 17+**（推荐 Eclipse Temurin 17）
- **Maven 3.9+**
- **Node.js 18+**
- **MySQL 8.0+**
- **Redis**（可选，部分功能依赖）

### 1. 初始化数据库

```bash
mysql -uroot -p < backend/src/main/resources/schema.sql
```

### 2. 配置

```bash
# 后端配置（可选，默认可直接运行）
vim backend/src/main/resources/application.yml

# AI 功能需要配置 API Key（可选，未配置时使用模板降级）
export AI_API_KEY=your-openai-api-key
```

### 3. 一键启动

```bash
chmod +x start.sh
./start.sh        # 开发模式
./start.sh build  # 构建前端生产包
```

### 4. 分步启动

**后端：**

```bash
cd backend
mvn package -DskipTests
java -jar target/timeweaver-backend-1.0.0.jar
```

**前端：**

```bash
cd frontend
npm install
npm run dev
```

### 5. 访问

- **前端**：http://localhost:5173（若端口被占用会自动递增，如 5174）
- **后端 API**：http://localhost:8080
- **AI 追踪日志**：`tail -f /tmp/tracker.log`
- **测试账号**：`aider` / `test123`

---

## Docker 部署

```bash
# AI API Key 可选，未配置时使用模板降级
AI_API_KEY=sk-xxx docker compose up -d
```

访问 http://localhost （Nginx 反向代理，推荐生产环境使用）。

---

## 项目结构

```
TimeWeaver/
├── backend/                         # Spring Boot 后端
│   ├── src/main/java/com/timeweaver/
│   │   ├── controller/              # REST 接口层（9 个 Controller）
│   │   ├── service/                 # 业务逻辑层
│   │   ├── repository/              # 数据访问层
│   │   ├── entity/                  # 数据库实体
│   │   ├── dto/                     # 数据传输对象
│   │   ├── ai/                      # Spring AI 集成
│   │   ├── job/                     # 定时任务
│   │   ├── websocket/               # WebSocket 处理器
│   │   ├── config/                  # 配置类
│   │   ├── common/                  # 公共（异常/常量/工具）
│   │   └── mapper/                  # MyBatis-Plus Mapper
│   └── src/main/resources/
│       ├── application.yml
│       └── schema.sql
│
├── frontend/                        # Vue 3 前端
│   └── src/
│       ├── views/                   # 页面组件
│       │   ├── login/               # 登录/注册
│       │   ├── dashboard/           # 首页概览
│       │   ├── records/             # 时间记录
│       │   ├── stats/               # 数据分析（ECharts）
│       │   ├── ai/                  # AI 会话追踪
│       │   ├── pomodoro/            # 番茄钟
│       │   ├── goals/               # 目标管理
│       │   ├── reports/             # AI 周报
│       │   └── profile/             # 个人信息
│       ├── components/              # 公共组件
│       ├── router/                  # 路由配置
│       ├── store/                   # Pinia 状态管理
│       └── api/                     # API 封装
│
├── docker-compose.yml               # Docker 编排
├── scripts/
│   └── ai-tracker.sh                 # AI 工具进程检测与自动记录脚本
├── start.sh                          # 一键启动脚本
└── PROJECT.md                        # 项目立项文档
```

---

## API 概览

| 模块 | 主要端点 | 说明 |
|------|----------|------|
| **认证** | `POST /api/auth/login` `POST /api/auth/register` | JWT 登录/注册 |
| **用户** | `GET/PUT /api/user/profile` `PUT /api/user/preferences` | 个人信息与偏好 |
| **时间记录** | `CRUD /api/records` | 计时/记录/编辑/删除 |
| **分类** | `CRUD /api/categories` | 系统预设 + 自定义 |
| **统计分析** | `GET /api/stats/*` | 日/周/月统计、趋势、对比 |
| **番茄钟** | `POST /api/pomodoros/*` | 开始/完成/中断/统计 |
| **目标** | `CRUD /api/goals` | 目标管理与进度追踪 |
| **AI 会话** | `CRUD /api/ai-sessions` `GET /api/ai-sessions/stats` | AI 工具会话追踪与统计 |
| **AI 周报** | `GET /api/reports/weekly` `POST /api/reports/.../generate` | 生成与获取周报 |
| **仪表盘** | `GET /api/dashboard/*` | 首页概览数据 |
| **WebSocket** | `ws://host/ws/notification?token={jwt}` | 实时通知 |

---

## 项目亮点

- **Spring AI 集成**：调用 ChatClient 生成结构化 JSON 周报，自动解析为 Java 对象
- **AI 降级策略**：未配置 LLM API Key 时自动使用模板引擎生成周报，保证功能可用
- **多维度可视化**：6 种 ECharts 图表（热力图/桑基图/雷达图等）覆盖不同分析视角
- **WebSocket 推送**：每个用户独立 session 管理，实现目标提醒与周报通知实时到达
- **时间预聚合**：按日/周/月多粒度预聚合，大数据量下查询 < 50ms
- **完整测试**：12 个单元测试覆盖核心 Service 层，Mockito + JUnit 5

---

## License

MIT
