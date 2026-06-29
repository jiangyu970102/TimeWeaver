-- TimeWeaver 数据库建表脚本
-- 使用前先创建数据库: CREATE DATABASE timeweaver DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    nickname        VARCHAR(50)  DEFAULT NULL,
    avatar          VARCHAR(255) DEFAULT NULL,
    email           VARCHAR(100) DEFAULT NULL,
    role            TINYINT      DEFAULT 0  COMMENT '0-普通用户, 1-管理员',
    status          TINYINT      DEFAULT 1  COMMENT '0-禁用, 1-正常',
    daily_goal_min  INT          DEFAULT 240 COMMENT '每日目标时长(分钟)',
    weekly_goal_min INT          DEFAULT 1200 COMMENT '每周目标时长(分钟)',
    pomodoro_duration INT       DEFAULT 25  COMMENT '番茄钟时长(分钟)',
    break_duration  INT          DEFAULT 5   COMMENT '休息时长(分钟)',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 分类表
CREATE TABLE IF NOT EXISTS t_category (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       DEFAULT NULL COMMENT 'NULL 表示系统预设分类',
    name            VARCHAR(50)  NOT NULL,
    icon            VARCHAR(50)  DEFAULT NULL COMMENT '图标标识符',
    color           VARCHAR(20)  DEFAULT NULL COMMENT '十六进制颜色 #FF6B6B',
    type            VARCHAR(20)  NOT NULL COMMENT 'work/study/entertain/rest/social/other',
    sort_order      INT          DEFAULT 0,
    is_system       TINYINT      DEFAULT 0  COMMENT '0-自定义, 1-系统预设',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 时间记录表（核心表）
CREATE TABLE IF NOT EXISTS t_time_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    category_id     BIGINT       NOT NULL,
    start_time      DATETIME     NOT NULL,
    end_time        DATETIME     DEFAULT NULL,
    duration_min    INT          DEFAULT NULL COMMENT '持续分钟数',
    description     VARCHAR(500) DEFAULT NULL,
    tags            VARCHAR(255) DEFAULT NULL COMMENT '逗号分隔标签',
    source          VARCHAR(20)  DEFAULT 'manual' COMMENT 'manual/pomodoro/auto',
    record_date     DATE         NOT NULL COMMENT '冗余字段，按天查询',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_user_category (user_id, category_id),
    INDEX idx_user_source (user_id, source),
    INDEX idx_record_date (record_date),
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_record_category FOREIGN KEY (category_id) REFERENCES t_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 番茄钟记录表
CREATE TABLE IF NOT EXISTS t_pomodoro (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    start_time      DATETIME     NOT NULL,
    end_time        DATETIME     DEFAULT NULL,
    duration_min    INT          DEFAULT 25,
    task_name       VARCHAR(200) DEFAULT NULL,
    task_description VARCHAR(500) DEFAULT NULL,
    completed       TINYINT      DEFAULT 1  COMMENT '0-中断/取消, 1-完成',
    tomato_count    INT          DEFAULT 1,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pomodoro_user (user_id),
    INDEX idx_pomodoro_start (start_time),
    CONSTRAINT fk_pomodoro_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 目标表
CREATE TABLE IF NOT EXISTS t_goal (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(100) NOT NULL,
    goal_type       VARCHAR(20)  NOT NULL COMMENT 'daily/weekly/monthly',
    category_id     BIGINT       DEFAULT NULL COMMENT 'NULL 表示所有分类',
    target_value    INT          NOT NULL COMMENT '目标值(分钟)',
    current_value   INT          DEFAULT 0  COMMENT '当前进度',
    unit            VARCHAR(20)  DEFAULT 'minutes',
    start_date      DATE         NOT NULL,
    end_date        DATE         DEFAULT NULL,
    status          TINYINT      DEFAULT 0  COMMENT '0-进行中, 1-已完成, 2-已放弃',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. AI 周报表
CREATE TABLE IF NOT EXISTS t_weekly_report (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    year            SMALLINT     NOT NULL,
    week            TINYINT      NOT NULL COMMENT '第几周(1-53)',
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    summary         TEXT         DEFAULT NULL COMMENT 'AI 生成周报正文',
    stats_snapshot  JSON         DEFAULT NULL COMMENT '统计快照',
    suggestions     JSON         DEFAULT NULL COMMENT 'AI 行动建议',
    strengths       JSON         DEFAULT NULL COMMENT '本周亮点',
    weaknesses      JSON         DEFAULT NULL COMMENT '待改进',
    report_status   VARCHAR(20)  DEFAULT 'pending' COMMENT 'pending/completed/failed',
    token_usage     INT          DEFAULT NULL COMMENT 'AI 调用 token 消耗',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_week (user_id, year, week),
    INDEX idx_user_year (user_id, year),
    CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 用户洞察表
CREATE TABLE IF NOT EXISTS t_user_insight (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    type            VARCHAR(30)  NOT NULL COMMENT 'weekly/monthly/achievement/milestone',
    title           VARCHAR(200) DEFAULT NULL,
    content         TEXT         DEFAULT NULL,
    is_read         TINYINT      DEFAULT 0,
    generated_at    DATETIME     DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read),
    CONSTRAINT fk_insight_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. AI 会话追踪表
CREATE TABLE IF NOT EXISTS t_ai_session (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    record_id       BIGINT       DEFAULT NULL COMMENT '关联的时间记录ID',
    tool_name       VARCHAR(50)  NOT NULL COMMENT 'claude-code/cursor/codex/chatgpt',
    model_name      VARCHAR(50)  NOT NULL COMMENT 'deepseek-v4-flash/gpt-4o/claude-sonnet/kimi-k2',
    start_time      DATETIME     NOT NULL,
    end_time        DATETIME     DEFAULT NULL,
    duration_sec    INT          DEFAULT NULL,
    token_count     INT          DEFAULT NULL,
    cost_estimate   DECIMAL(10,6) DEFAULT NULL,
    session_meta    JSON         DEFAULT NULL COMMENT '额外元数据(文件列表/对话摘要等)',
    source          VARCHAR(20)  DEFAULT 'manual' COMMENT 'manual/auto/detect',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, start_time),
    INDEX idx_tool_model (tool_name, model_name),
    CONSTRAINT fk_ai_session_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_ai_session_record FOREIGN KEY (record_id) REFERENCES t_time_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Git 仓库配置表
CREATE TABLE IF NOT EXISTS t_git_repo_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    repo_path       VARCHAR(500) NOT NULL COMMENT '本地仓库绝对路径',
    repo_name       VARCHAR(100) DEFAULT NULL COMMENT '仓库名称（自动提取）',
    auto_import     TINYINT      DEFAULT 0 COMMENT '0-手动, 1-自动导入为时间记录',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    CONSTRAINT fk_git_config_user FOREIGN KEY (user_id) REFERENCES t_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Git 提交记录表
CREATE TABLE IF NOT EXISTS t_git_commit_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    repo_path       VARCHAR(100) DEFAULT NULL COMMENT '仓库名（冗余，便于展示）',
    commit_hash     VARCHAR(64)  NOT NULL,
    author_name     VARCHAR(100) DEFAULT NULL,
    message         TEXT         DEFAULT NULL,
    committed_at    DATETIME     NOT NULL,
    record_id       BIGINT       DEFAULT NULL COMMENT '关联的时间记录ID（导入后非空）',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, committed_at),
    INDEX idx_hash (user_id, commit_hash),
    CONSTRAINT fk_git_commit_user FOREIGN KEY (user_id) REFERENCES t_user(id),
    CONSTRAINT fk_git_commit_record FOREIGN KEY (record_id) REFERENCES t_time_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 系统预设分类数据
-- ============================================
INSERT INTO t_category (user_id, name, icon, color, type, sort_order, is_system) VALUES
(NULL, '开发·编码',     'code',     '#4A90D9', 'work',     1,  1),
(NULL, '开发·调试',     'bug',      '#E74C3C', 'work',     2,  1),
(NULL, '开发·Code Review', 'review', '#8E44AD', 'work',   3,  1),
(NULL, '会议',          'meeting',  '#F39C12', 'work',     4,  1),
(NULL, '文档·写作',     'document', '#2ECC71', 'work',     5,  1),
(NULL, '学习·阅读',     'book',     '#1ABC9C', 'study',    6,  1),
(NULL, '学习·课程',     'course',   '#3498DB', 'study',    7,  1),
(NULL, '学习·实践',     'practice', '#9B59B6', 'study',    8,  1),
(NULL, '娱乐·短视频',   'video',    '#E91E63', 'entertain', 9, 1),
(NULL, '娱乐·游戏',     'game',     '#FF5722', 'entertain',10, 1),
(NULL, '娱乐·社交',     'social',   '#2196F3', 'social',   11, 1),
(NULL, '休息·午休',     'nap',      '#607D8B', 'rest',     12, 1),
(NULL, '休息·通勤',     'commute',  '#795548', 'rest',     13, 1),
(NULL, '运动·锻炼',     'sport',    '#4CAF50', 'rest',     14, 1),
(NULL, '其他',          'more',     '#9E9E9E', 'other',    99, 1);
