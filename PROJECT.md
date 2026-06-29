# TimeWeaver — 智能时间投资分析平台 · 项目立项文档

## 一、项目概述

| 项目名称 | TimeWeaver — 智能时间投资分析平台 |
|---|---|
| 项目类型 | Web 应用（前后端分离） |
| 项目目标 | 求职作品，展示后端工程能力 + AI 集成 + 系统设计 |
| 核心定位 | 面向程序员/知识工作者的时间追踪与 AI 效率分析平台 |

### 一句话描述

> 像记账一样记录时间，AI 分析你的效率模式并给出可执行的改进建议。

---

## 二、项目背景与痛点

### 市场痛点

1. **"忙了一天但不知道忙了什么"** — 程序员和知识工作者的普遍焦虑，缺乏对时间分配的量化认知
2. **现有工具只记录，不分析** — RescueTime 偏监控、Toggl 偏项目管理、Forest 只是倒计时，**没有一个工具形成"记录 → 分析 → 建议 → 改进"的闭环**
3. **缺少 AI 驱动的个性化洞察** — 传统时间管理工具只有图表展示，不会告诉你"你周三下午效率最高，建议把核心任务安排在这个时段"

### 与竞品的差异

| 产品 | 定位 | TimeWeaver 的差异化 |
|---|---|---|
| RescueTime | 自动追踪，偏监控 | AI 分析 + 目标驱动，不只是"看数据" |
| Toggl | 手动计时，偏项目管理 | 偏向个人效率，番茄钟 + 分析闭环 |
| Forest | 专注计时，游戏化 | 完整分析平台，不只是倒计时 |
| aTimeLogger | 纯记录 | AI 周报 + 行动建议，不止是记录 |

### 目标用户

| 用户画像 | 核心需求 |
|---|---|
| 程序员 / 开发者 | 追踪 coding 时间，减少会议和摸鱼，优化效率 |
| 自由职业者 / 远程工作者 | 按项目拆分工时，避免工作时间无限延长 |
| 备考学生（考研/考公） | 追踪各科目学习时间，优化时间分配 |
| 知识工作者（产品/运营） | 回答"这个月在哪个项目上花了多少时间"，辅助述职 |

---

## 三、核心功能模块

### 模块 1：用户模块

- 注册 / 登录（JWT 鉴权）
- 个人信息维护
- 效率偏好设置（默认分类、每日目标时长、番茄钟时长）

### 模块 2：时间记录模块（核心）

手动记录：

- 开始/停止计时（一键记录当前活动）
- 选择分类 + 标签 + 描述
- 回溯补录（忘记记了可以补填）

番茄钟模式：

- 专注 25 分钟 / 休息 5 分钟
- 完成番茄自动记录到时间轴
- 番茄统计（今日完成数、连续天数）

我的时间轴：

- 按日期展示全天时间线
- 每条记录可编辑/删除
- 彩色分类标签，一目了然

### 模块 3：分类管理

- 系统预设分类（工作·开发、工作·会议、学习·阅读、学习·课程、生活·休息、生活·通勤、娱乐·摸鱼 等）
- 用户自定义分类（自定义名称 + 图标 + 颜色）
- 分类统计占比

### 模块 4：数据分析仪表盘（核心，ECharts 可视化）

| 图表 | 展示内容 | ECharts 组件 |
|---|---|---|
| 日历热力图 | 每日各时段活动分布 | `heatmap` |
| 环形图 / 饼图 | 各分类时间占比 | `pie` |
| 折线图 | 日/周高效时长趋势 | `line` |
| 桑基图 | 时间流向：分类 → 子活动 → 占比 | `sankey` |
| 雷达图 | 多维度效率评估（专注度/均衡性/持续性） | `radar` |
| 柱状图 | 本周 vs 上周分类耗时对比 | `bar` |

### 模块 5：AI 智能周报（核心差异化，Spring AI）

- 每周日自动生成（Spring Schedule 定时触发）
- 也可以手动触发生成
- 报告包含：
  - **本周概览**：总记录时长、高效 coding 时长、摸鱼时长
  - **分类占比**：各类活动的时间投入分布
  - **趋势对比**：与上周关键指标的变化
  - **AI 洞察**：发现用户的效率模式（如"上午效率高于下午"）
  - **行动建议**：可执行的改进建议（如"建议把会议挪到下午"）
  - **成就徽章**：达成目标自动颁发

### 模块 6：目标管理

- 设定日/周/月目标（如"每日高效 coding 4 小时"）
- 实时进度追踪（仪表盘展示进度条）
- 目标达成提醒（WebSocket 实时推送）
- 目标历史记录

### 模块 7：AI 会话追踪（新增 · 已实现）

- **自动检测** — 本地守护进程 `scripts/ai-tracker.sh` 通过进程名和窗口检测识别活跃 AI 工具
- **支持的工具** — Claude Code、Cursor、Codex、ChatGPT、Kimi
- **会话生命周期管理** — 自动开始/结束（5 分钟超时阈值），记录持续时间
- **可视化面板** — 工具分布饼图、模型分布饼图、每日趋势柱状图
- **手动管理** — 支持补录、编辑、删除会话记录
- **自动刷新** — 30 秒轮询获取最新数据

### 模块 8：实时通知（WebSocket）

- 目标达成/偏离提醒
- 摸鱼提醒（可选）
- 番茄钟完成提醒
- 周报生成完成推送

---

## 四、技术选型

### 后端技术栈

| 技术 | 用途 | 重要性 |
|---|---|---|
| **Spring Boot 3.x** | 项目骨架，IoC 容器，自动配置 | 必备 |
| **Spring Security + JWT** | 用户认证与授权 | 必备 |
| **MyBatis-Plus** | ORM，数据访问层，代码生成 | 必备 |
| **MySQL 8.x** | 数据持久化 | 必备 |
| **Redis** | 缓存（日活统计/热数据）、分布式限流 | 必备 |
| **Spring AI** | 调用 LLM 生成结构化 AI 周报/建议 | **核心** |
| **Spring Schedule** | 定时任务（周报自动生成、数据预聚合） | 必备 |
| **WebSocket** | 实时推送通知 | 必备 |
| **Spring Validation** | 参数校验 | 标配 |
| **Lombok** | 减少样板代码 | 标配 |

### 前端技术栈

| 技术 | 用途 |
|---|---|
| **Vue 3 + Vite** | 前端框架 + 构建工具 |
| **Element Plus** | UI 组件库 |
| **Axios** | HTTP 请求 |
| **Pinia** | 状态管理 |
| **Vue Router** | 前端路由 |
| **ECharts** | 数据可视化（项目核心展示层） |
| **Day.js** | 日期处理 |

### 为什么选这些技术

| 选型决策 | 理由 |
|---|---|
| **Spring AI 而非直接调 HTTP API** | Spring 官方生态，展示对主流框架趋势的关注；结构化输出解析（`ChatClient.call().entity()`）比手动解析 JSON 优雅得多 |
| **MySQL 而非 SQL Server** | 互联网行业更通用，面试官认可度更高 |
| **Redis 从"可选"变"必备"** | 承担日活统计、热数据缓存、接口限流三个职责，体现缓存意识 |

---

## 五、数据库设计

### E-R 图概览

```
User (1) ──→ (N) TimeRecord
User (1) ──→ (N) Category
User (1) ──→ (N) Goal
User (1) ──→ (N) WeeklyReport
User (1) ──→ (N) Pomodoro
User (1) ──→ (N) UserInsight
```

### 核心表设计

```sql
-- 1. 用户表
CREATE TABLE t_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    nickname        VARCHAR(50),
    avatar          VARCHAR(255),
    email           VARCHAR(100),
    role            TINYINT DEFAULT 0 COMMENT '0-普通用户, 1-管理员',
    status          TINYINT DEFAULT 1 COMMENT '0-禁用, 1-正常',
    daily_goal_min  INT DEFAULT 240 COMMENT '每日目标时长(分钟)',
    weekly_goal_min INT DEFAULT 1200 COMMENT '每周目标时长(分钟)',
    pomodoro_duration INT DEFAULT 25 COMMENT '番茄钟时长(分钟)',
    break_duration  INT DEFAULT 5 COMMENT '休息时长(分钟)',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. 分类表（系统预设 + 用户自定义）
CREATE TABLE t_category (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT COMMENT 'NULL 表示系统预设分类',
    name            VARCHAR(50) NOT NULL,
    icon            VARCHAR(50) COMMENT '图标标识符',
    color           VARCHAR(20) COMMENT '十六进制颜色 #FF6B6B',
    type            VARCHAR(20) NOT NULL COMMENT 'work/study/entertain/rest/social/other',
    sort_order      INT DEFAULT 0,
    is_system       TINYINT DEFAULT 0 COMMENT '0-自定义, 1-系统预设',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 时间记录表（核心表，数据量最大）
CREATE TABLE t_time_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    category_id     BIGINT NOT NULL,
    start_time      DATETIME NOT NULL,
    end_time        DATETIME,
    duration_min    INT COMMENT '持续分钟数',
    description     VARCHAR(500),
    tags            VARCHAR(255) COMMENT '逗号分隔标签',
    source          VARCHAR(20) DEFAULT 'manual' COMMENT 'manual/pomodoro/auto',
    record_date     DATE NOT NULL COMMENT '冗余字段，方便按天查询',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_user_category (user_id, category_id),
    INDEX idx_user_start (user_id, start_time),
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_record_category FOREIGN KEY (category_id) REFERENCES t_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 番茄钟记录表
CREATE TABLE t_pomodoro (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    start_time      DATETIME NOT NULL,
    end_time        DATETIME,
    duration_min    INT DEFAULT 25,
    task_name       VARCHAR(200),
    task_description VARCHAR(500),
    completed       TINYINT DEFAULT 1 COMMENT '0-中断/取消, 1-完成',
    tomato_count    INT DEFAULT 1,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, DATE(start_time)),
    CONSTRAINT fk_pomodoro_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 目标表
CREATE TABLE t_goal (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    name            VARCHAR(100) NOT NULL,
    goal_type       VARCHAR(20) NOT NULL COMMENT 'daily/weekly/monthly',
    category_id     BIGINT COMMENT 'NULL 表示所有分类',
    target_value    INT NOT NULL COMMENT '目标值(分钟)',
    current_value   INT DEFAULT 0 COMMENT '当前进度',
    unit            VARCHAR(20) DEFAULT 'minutes',
    start_date      DATE NOT NULL,
    end_date        DATE,
    status          TINYINT DEFAULT 0 COMMENT '0-进行中, 1-已完成, 2-已放弃',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. AI 周报表
CREATE TABLE t_weekly_report (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    year            SMALLINT NOT NULL,
    week            TINYINT NOT NULL COMMENT '第几周(1-53)',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    summary         TEXT COMMENT 'AI 生成周报正文',
    stats_snapshot  JSON COMMENT '统计快照(各分类时长、趋势等)',
    suggestions     JSON COMMENT 'AI 行动建议列表',
    strengths       JSON COMMENT '本周亮点',
    weaknesses      JSON COMMENT '待改进',
    report_status   VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/completed/failed',
    token_usage     INT COMMENT 'AI 调用 token 消耗',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_week (user_id, year, week),
    INDEX idx_user_year (user_id, year),
    CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. 用户洞察表（AI 推送的各类建议/提醒）
CREATE TABLE t_user_insight (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    type            VARCHAR(30) NOT NULL COMMENT 'weekly/monthly/achievement/milestone',
    title           VARCHAR(200),
    content         TEXT,
    is_read         TINYINT DEFAULT 0,
    generated_at    DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read),
    CONSTRAINT fk_insight_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 六、系统架构设计

### 分层架构

```
┌─────────────────────────────────────────────────┐
│                  Frontend (Vue3)                  │
│  Element Plus UI  │  ECharts 图表  │  Pinia 状态  │
└──────────────────┬──────────────────────────────┘
                   │ HTTP/WebSocket
┌──────────────────▼──────────────────────────────┐
│               Nginx (反向代理)                     │
└──────────────────┬──────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────┐
│           Controller 层 (REST API)                │
│  AuthController │ RecordController │ Report...    │
├─────────────────────────────────────────────────┤
│              Service 层 (业务逻辑)                  │
│  TimeRecordService  │  AnalysisService             │
│  GoalService        │  WeeklyReportService         │
│  PomodoroService    │  AIService                   │
├─────────────────────────────────────────────────┤
│              Repository 层 (数据访问)                │
│              MyBatis-Plus BaseMapper                │
├─────────────────────────────────────────────────┤
│     MySQL  │  Redis  │  LLM API (外部)            │
└─────────────────────────────────────────────────┘
```

### 数据流：AI 周报生成

```
Schedule (每周日 20:00 触发)
    │
    ▼
WeeklyReportService.generate(userId)
    │
    ├── 1. 查询 t_time_record 聚合本周数据
    ├── 2. 查询上周 report 获取 baseline
    ├── 3. 构造 Prompt 上下文
    │      ├── 本周各分类时长统计
    │      ├── 与上周对比变化
    │      ├── 当前目标完成进度
    │      └── 用户自定义偏好
    │
    ├── 4. 调用 Spring AI ChatClient
    │      └── LLM 返回结构化 JSON
    │
    ├── 5. 解析结果存入 t_weekly_report
    ├── 6. 写入 t_user_insight（重要发现推送）
    └── 7. WebSocket 通知前端"周报已生成"
```

---

## 七、API 接口概览

### 认证模块

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录，返回 JWT |
| POST | `/api/auth/refresh` | 刷新 Token |

### 用户模块

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/user/profile` | 获取个人信息 |
| PUT | `/api/user/profile` | 更新个人信息 |
| PUT | `/api/user/preferences` | 更新效率偏好 |

### 时间记录模块

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/records` | 开始计时 / 手动记录 |
| PUT | `/api/records/{id}/stop` | 停止计时 |
| PUT | `/api/records/{id}` | 编辑记录 |
| DELETE | `/api/records/{id}` | 删除记录 |
| GET | `/api/records?date=&page=&size=` | 按日期查询记录列表 |
| GET | `/api/records/current` | 获取当前正在进行的记录 |

### 统计分析模块

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/stats/daily?date=` | 日度统计 |
| GET | `/api/stats/weekly?year=&week=` | 周度统计 |
| GET | `/api/stats/monthly?year=&month=` | 月度统计 |
| GET | `/api/stats/heatmap?year=&month=` | 日历热力图数据 |
| GET | `/api/stats/trend?start=&end=&granularity=` | 趋势数据 |
| GET | `/api/stats/comparison?start=&end=` | 时间段对比 |

### 分类模块

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/categories` | 获取分类列表（含系统预设和自定义） |
| POST | `/api/categories` | 创建自定义分类 |
| PUT | `/api/categories/{id}` | 修改分类 |
| DELETE | `/api/categories/{id}` | 删除自定义分类 |

### 番茄钟模块

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/pomodoros/start` | 开始番茄钟 |
| PUT | `/api/pomodoros/{id}/complete` | 完成番茄 |
| PUT | `/api/pomodoros/{id}/cancel` | 中断番茄 |
| GET | `/api/pomodoros/stats` | 番茄统计 |

### 目标模块

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/goals` | 目标列表 |
| POST | `/api/goals` | 创建目标 |
| PUT | `/api/goals/{id}` | 更新目标 |
| DELETE | `/api/goals/{id}` | 删除目标 |
| GET | `/api/goals/progress` | 当前进度概览 |

### AI 周报模块

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/reports/weekly?year=&week=` | 获取周报 |
| POST | `/api/reports/weekly/generate` | 手动触发 AI 生成 |
| GET | `/api/reports/list?page=&size=` | 周报历史列表 |

### AI 会话追踪模块

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/ai-sessions?startDate=&endDate=` | 按日期范围查询会话列表 |
| GET | `/api/ai-sessions/stats?startDate=&endDate=` | 统计汇总（工具/模型分布、每日趋势） |
| GET | `/api/ai-sessions/{id}` | 获取单条会话详情 |
| POST | `/api/ai-sessions` | 创建会话（手动/自动检测） |
| PUT | `/api/ai-sessions/{id}` | 更新会话（结束时间、时长等） |
| DELETE | `/api/ai-sessions/{id}` | 删除会话 |

### 仪表盘

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/dashboard/overview` | 首页概览数据 |
| GET | `/api/dashboard/today` | 今日时间线 |

### WebSocket

| 端点 | 说明 |
|---|---|
| `/ws/notification?token={jwt}` | 接收实时通知 |

---

## 八、项目目录结构

```
TimeWeaver/
├── PROJECT.md                         # 项目立项文档（本文件）
│
├── scripts/
│   └── ai-tracker.sh                 # AI 工具进程检测与自动记录脚本
│
├── docs/                              # 设计文档
│   ├── database-design.md             # 数据库详细设计
│   ├── api-design.md                  # API 接口规范
│   └── architecture.md                # 架构设计
│
├── backend/                           # Spring Boot 后端
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/timeweaver/
│       │   │   ├── TimeWeaverApplication.java
│       │   │   │
│       │   │   ├── controller/            # REST 接口层
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── UserController.java
│       │   │   │   ├── RecordController.java
│       │   │   │   ├── CategoryController.java
│       │   │   │   ├── StatsController.java
│       │   │   │   ├── PomodoroController.java
│       │   │   │   ├── GoalController.java
│       │   │   │   ├── ReportController.java
│       │   │   │   ├── DashboardController.java
│       │   │   │   └── AiSessionController.java
│       │   │   │
│       │   │   ├── service/               # 业务逻辑层
│       │   │   │   ├── AuthService.java
│       │   │   │   ├── UserService.java
│       │   │   │   ├── TimeRecordService.java
│       │   │   │   ├── CategoryService.java
│       │   │   │   ├── StatsService.java
│       │   │   │   ├── PomodoroService.java
│       │   │   │   ├── GoalService.java
│       │   │   │   ├── WeeklyReportService.java
│       │   │   │   ├── AiSessionService.java
│       │   │   │   └── NotificationService.java
│       │   │   │
│       │   │   ├── repository/            # 数据访问层
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── TimeRecordRepository.java
│       │   │   │   ├── CategoryRepository.java
│       │   │   │   ├── PomodoroRepository.java
│       │   │   │   ├── GoalRepository.java
│       │   │   │   ├── WeeklyReportRepository.java
│       │   │   │   └── AiSessionRepository.java
│       │   │   │
│       │   │   ├── entity/                # 数据库实体
│       │   │   │   ├── User.java
│       │   │   │   ├── TimeRecord.java
│       │   │   │   ├── Category.java
│       │   │   │   ├── Pomodoro.java
│       │   │   │   ├── Goal.java
│       │   │   │   ├── WeeklyReport.java
│       │   │   │   └── AiSession.java
│       │   │   │
│       │   │   ├── dto/                   # 数据传输对象
│       │   │   │   ├── request/
│       │   │   │   │   ├── LoginRequest.java
│       │   │   │   │   ├── RegisterRequest.java
│       │   │   │   │   ├── RecordCreateRequest.java
│       │   │   │   │   ├── GoalCreateRequest.java
│       │   │   │   │   └── ...
│       │   │   │   └── response/
│       │   │   │       ├── ApiResponse.java
│       │   │   │       ├── DashboardVO.java
│       │   │   │       ├── WeeklyReportVO.java
│       │   │   │       └── ...
│       │   │   │
│       │   │   ├── ai/                    # AI 相关
│       │   │   │   ├── AIService.java         # Spring AI 封装
│       │   │   │   ├── ReportPromptTemplate.java  # 提示词模板
│       │   │   │   └── dto/
│       │   │   │       └── AIReportDTO.java
│       │   │   │
│       │   │   ├── job/                   # 定时任务
│       │   │   │   └── WeeklyReportJob.java
│       │   │   │
│       │   │   ├── websocket/             # WebSocket
│       │   │   │   ├── WebSocketConfig.java
│       │   │   │   └── NotificationHandler.java
│       │   │   │
│       │   │   ├── config/                # 配置
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── RedisConfig.java
│       │   │   │   ├── CorsConfig.java
│       │   │   │   └── AppConfig.java
│       │   │   │
│       │   │   ├── common/                # 公共
│       │   │   │   ├── exception/
│       │   │   │   ├── constant/
│       │   │   │   └── utils/
│       │   │   │
│       │   │   └── mapper/                # MyBatis-Plus Mapper
│       │   │       ├── UserMapper.java
│       │   │       ├── TimeRecordMapper.java
│       │   │       └── ...
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-prod.yml
│       │       └── mapper/
│       │
│       └── test/
│           └── java/com/timeweaver/
│               ├── service/
│               │   └── WeeklyReportServiceTest.java
│               └── controller/
│                   └── RecordControllerTest.java
│
└── frontend/                           # Vue3 前端
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.js
        ├── App.vue
        │
        ├── router/
        │   └── index.js
        │
        ├── store/
        │   ├── user.js
        │   ├── records.js
        │   └── dashboard.js
        │
        ├── api/
        │   ├── auth.js
        │   ├── user.js
        │   ├── records.js
        │   ├── stats.js
        │   ├── goals.js
        │   ├── reports.js
        │   └── ai-sessions.js
        │
        ├── views/
        │   ├── login/
        │   │   ├── Login.vue
        │   │   └── Register.vue
        │   ├── dashboard/
        │   │   ├── Dashboard.vue           # 首页概览
        │   │   ├── TimeLine.vue            # 今日时间轴
        │   │   └── QuickRecord.vue         # 快速记录
        │   ├── records/
        │   │   ├── RecordList.vue          # 时间记录列表
        │   │   └── RecordEdit.vue          # 编辑记录
        │   ├── stats/
        │   │   ├── StatsDashboard.vue      # 统计分析（ECharts）
        │   │   ├── HeatmapView.vue         # 日历热力图
        │   │   ├── TrendView.vue           # 趋势分析
        │   │   └── ComparisonView.vue      # 对比分析
        │   ├── pomodoro/
        │   │   └── PomodoroView.vue        # 番茄钟
        │   ├── goals/
        │   │   ├── GoalList.vue
        │   │   └── GoalEdit.vue
        │   ├── reports/
        │   │   └── WeeklyReport.vue        # AI 周报
        │   ├── profile/
        │   │   └── Profile.vue             # 个人信息
        │   └── admin/
        │       └── AdminDashboard.vue      # 管理后台
        │
        ├── components/
        │   ├── common/
        │   │   ├── NavBar.vue
        │   │   ├── SideBar.vue
        │   │   └── CategoryPicker.vue
        │   └── charts/
        │       ├── HeatMap.vue
        │       ├── PieChart.vue
        │       ├── LineChart.vue
        │       ├── SankeyChart.vue
        │       └── RadarChart.vue
        │
        ├── utils/
        │   ├── request.js           # Axios 封装
        │   ├── auth.js              # Token 管理
        │   └── format.js            # 格式化工具
        │
        └── assets/
            └── styles/
```

---

## 九、开发计划（里程碑）

| 阶段 | 内容 | 产出 | 状态 |
|---|---|---|---|
| **1. 项目初始化** | Spring Boot 项目搭建 + 数据库建表 + Vue 项目初始化 + 基础配置 | 前后端项目骨架 | ✓ 已完成 |
| **2. 用户模块** | 注册/登录 + JWT 鉴权 + 用户信息维护 | 认证流程打通前后端 | ✓ 已完成 |
| **3. 分类管理** | 系统预设分类 + 自定义分类 CRUD | 分类模块 | ✓ 已完成 |
| **4. 时间记录** | 手动记录 + 开始/停止 + 列表展示 + 编辑删除 | 核心记录功能 | ✓ 已完成 |
| **5. 番茄钟** | 番茄钟计时 + 完成/中断 + 关联记录 | 番茄钟模块 | ✓ 已完成 |
| **6. 数据分析仪表盘** | ECharts 各图表集成 + 聚合查询接口 | 可视化分析 | ✓ 已完成 |
| **7. 目标管理** | 目标 CRUD + 进度追踪 + 自动更新 | 目标模块 | ✓ 已完成 |
| **8. AI 会话追踪** | 本地进程检测 + 自动记录 + 图表可视化 + 手动管理 | AI 工具使用追踪 | ✓ 已完成 |
| **9. AI 周报** | Spring AI 集成 + 提示词模板 + 定时任务 + WebSocket 推送 | **核心差异化功能** | - 进行中 |
| **10. 通知与优化** | WebSocket 通知 + 接口优化 + 异常处理 | 完善体验 | - 待开发 |
| **11. 测试与部署** | 单元测试 + 联调 + Docker 部署配置 | 可演示版本 | - 待开发 |

**总计预估：约 22 天（每晚 + 周末，约 1.5 个月）**

---

## 十、面试亮点设计

### 面试官可能会问的问题 & 你的回答提纲

| 面试官问题 | 回答方向 |
|---|---|
| **为什么做这个项目？** | "我自己就有时间管理的痛点，发现现有工具只记录不分析，所以做了这个带 AI 分析闭环的平台" |
| **Spring AI 怎么用的？** | 调用 ChatClient，定义 Java record 作为输出结构，StructuredOutput 自动解析 JSON |
| **AI 提示词怎么设计的？** | 提供本周聚合数据 + 上周 baseline，要求 LLM 输出结构化 JSON（含总结/建议/趋势） |
| **周报生成性能怎么保证？** | 异步执行 + Redis Stream 任务队列，前端轮询或 WebSocket 接收完成通知 |
| **数据聚合怎么做？** | 预聚合表（日/周聚合），定时任务凌晨计算，避免实时联表查询 |
| **ECharts 用了哪些图？** | 热力图、桑基图、雷达图——不是柱状图凑数，而是每个图对应一个分析视角 |
| **Redis 除了缓存还干嘛？** | 日活统计（HyperLogLog）、接口限流（滑动窗口）、正在进行的记录暂存 |
| **项目最大挑战？** | AI 输出的结构化解析——LLM 偶尔返回格式不对，加了重试 + 降级策略 |

### 简历写法示例

> **TimeWeaver — 智能时间投资分析平台**
>
> 基于 Spring Boot 3 + Spring AI + Vue3 的全栈项目，面向程序员的时间追踪与 AI 效率分析平台。
>
> - 设计并实现 7 张核心数据库表，覆盖时间记录、分类、目标、AI 报告等业务实体
> - 集成 **Spring AI** 调用 LLM 生成结构化周报，实现自动重试与降级策略
> - 实现时序数据按日/周/月多粒度 **预聚合**，单用户年数据量下查询 < 50ms
> - 基于 ECharts 实现 6 种可视化图表（热力图/桑基图/雷达图等），展示时间分配洞察
> - 使用 Redis HyperLogLog 统计日活 + 滑动窗口限流，保障接口稳定性
> - WebSocket 实时推送目标进度与周报生成通知

---

## 十一、与二手交易平台对比总结

| 对比维度 | 校园二手交易（原方案） | TimeWeaver（推荐方案） |
|---|---|---|
| **新颖度** | ⭐⭐ 烂大街 | ⭐⭐⭐⭐ 少有同类型 |
| **技术深度** | ⭐⭐ CRUD 为主 | ⭐⭐⭐⭐ AI + 聚合 + 可视化 |
| **面试共鸣** | ⭐ "又一个商城" | ⭐⭐⭐⭐⭐ 面试官也是目标用户 |
| **前端表现力** | ⭐⭐ 列表 + 表单 | ⭐⭐⭐⭐⭐ 6 种 ECharts 图表 |
| **AI 集成** | ❌ 无 | ✅ Spring AI 核心集成 |
| **可量化指标** | 难量化 | 可以（查询耗时、Token 数、图表类型） |
| **代码复用度** | 零（新项目） | **80% 技术栈复用你原计划** |
| **学习成本** | — | 只需学 Spring AI，其他都会 |
| **开发工期** | 约 30 天 | 约 22 天 |

---

## 十二、待定事项

- [ ] LLM 选型：DeepSeek / 通义千问 / OpenAI（考虑成本和稳定性）
- [ ] Docker 部署方案
- [ ] 是否需要移动端适配
- [ ] 是否需要浏览器插件自动追踪（可选，二期）

---

> 本文档版本: v1.1
> 最后更新: 2026-06-29
