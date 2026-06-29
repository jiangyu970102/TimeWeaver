# 开发踩坑记录

> AI 辅助生成项目过程中遇到的典型问题与教训总结。

---

## 一、后端

### 1. CORS 配置端口硬编码

**问题**：前端 Vite 端口从 5173 自动递增到 5174 后，登录接口返回 403。

```
Access to XMLHttpRequest at 'http://localhost:8080/api/auth/login' 
from origin 'http://localhost:5174' has been blocked by CORS policy
```

**根因**：`SecurityConfig` 中写死了 `setAllowedOrigins(List.of("http://localhost:5173"))`，未考虑端口变更。

**解决**：改用 `setAllowedOriginPatterns(List.of("http://localhost:*"))` 匹配任意端口。

**教训**：涉及跨域配置时，优先用通配模式而非硬编码端口，尤其开发环境端口可能被占用递增。

---

### 2. Spring AI 强制依赖 API Key

**问题**：后端启动失败，报错 `OpenAI API key must be set`。

```
Caused by: java.lang.IllegalArgumentException: OpenAI API key must be set.
Use the connection property: spring.ai.openai.api-key or spring.ai.openai.chat.api-key property.
```

**根因**：`application-dev.yml` 中 `spring.ai.openai.api-key: ${AI_API_KEY:}` 默认值为空字符串，Spring AI 自动配置拒绝空 key。即使未使用 AI 功能，容器启动时也会校验。

**解决**：启动时设置环境变量 `AI_API_KEY=sk-placeholder`。

**教训**：使用 Spring AI 时需注意其自动配置的强制校验。更好的做法是使用 `@ConditionalOnProperty` 将 AI 功能做成可选模块，或提供一个虚假占位 key。

---

### 3. MyBatis-Plus LambdaQueryWrapper API 不熟

**问题**：编译报错，`LambdaQueryWrapper.<T>queryWrapper()` 方法不存在。

```
Cannot resolve method 'queryWrapper'
```

**根因**：混淆了 MyBatis-Plus 的两种静态工厂方式——`LambdaQueryWrapper` 构造器与 `Wrappers.lambdaQuery()` 静态方法的区别。

**解决**：统一使用 `Wrappers.<T>lambdaQuery()`。

**教训**：使用框架时先确认 API 的正确调用方式，不要凭记忆写。MyBatis-Plus 3.x 的推荐写法是 `Wrappers.lambdaQuery()`。

---

### 4. Service 层方法不存在

**问题**：`GitService` 中调用 `TimeRecordService.createRecord()` 编译报错，该方法实际不存在。

**根因**：假设了 Service 层有某个方法，但未验证实际代码就直接引用。

**解决**：改为直接注入 `TimeRecordMapper` 调用 `insert()`。

**教训**：跨 Service 调用前应先确认方法签名，避免假设。或者直接使用 Mapper 层操作。

---

### 5. Maven 版本过旧

**问题**：打包时报错 `The plugin ... requires Maven version 3.6.3`。

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.13.0:compile
The plugin org.apache.maven.plugins:maven-compiler-plugin:3.13.0 requires Maven version 3.6.3
```

**根因**：系统默认 Maven 为 3.6.1，而 Spring Boot 3.x 依赖的 compiler plugin 需要 3.6.3+。

**解决**：使用 Homebrew 安装的 Maven 3.9.16 替代系统默认版本。

**教训**：Spring Boot 3 项目应确保 Maven 版本 >= 3.6.3。建议使用 Maven Wrapper（`mvnw`）避免环境不一致。

---

## 二、前端

### 1. HTML 标签未闭合

**问题**：Vite 构建时报错 `Invalid end tag`。

```
[plugin:vite:vue] Invalid end tag
```

**根因**：`<el-table` 标签缺少闭合的 `>`，Vue 模板解析器无法正确识别。

**解决**：补全标签闭合符号。

**教训**：Vue 模板是 HTML 的超集，标签结构必须严格正确。编辑器语法高亮和 ESLint 可以在早期发现此类问题。

---

### 2. 重构遗留悬挂引用

**问题**：删除 token/cost 相关图表后，饼图全部渲染空白，控制台无报错但图表不显示。

**根因**：删除了 `costChart` 变量但未删除 `costChart?.dispose()` 调用。JavaScript 在未定义变量上调用方法抛出 `ReferenceError`，终止了整个 `renderCharts()` 函数的执行，导致后续工具/模型饼图也未被渲染。

```javascript
// 删除了 const costChart = ... 但遗留了：
costChart?.dispose()  // → ReferenceError: costChart is not defined
```

**解决**：移除废弃的 dispose 调用。

**教训**：重构删除变量时，需同时清理所有引用。ECharts 的 dispose 调用在图表组件销毁和重渲染时尤其容易遗漏。切换到 TypeScript 可以避免这类问题（未定义变量直接编译报错）。

---

### 3. 页面布局过长未做分页

**问题**：AI 会话页面内容过多需要大幅滚动才能看到底部。

**根因**：一次性加载所有 AI 会话并平铺展示，没有对长列表做分页或虚拟滚动。

**解决**：改用 `el-pagination` 分页组件，每页固定条数。

**教训**：列表类展示应默认考虑分页，数据量增长后页面性能会急剧下降。

---

## 三、Git 与版本控制

### 1. GitHub Fine-grained PAT 权限不足

**问题**：使用 `github_pat_*` 令牌推送时失败，报错 403 或 `HTTP2 framing layer`。

**根因**：Fine-grained Personal Access Token 需要显式授权仓库，且对新建仓库支持不完善。Classic PAT 没有此限制。

**解决**：改用 Classic PAT（`ghp_*`）。

**教训**：GitHub 的两种 PAT 类型有不同权限模型。CLI 操作推荐使用 Classic PAT 或 `gh auth login` 登录后操作。

---

### 2. `gh` CLI 在后台任务中不在 PATH

**问题**：后台运行的 shell 命令找不到 `gh` 命令。

```
command not found: gh
```

**根因**：后台任务的环境变量与交互式 shell 不同，`gh` 安装在 Homebrew 目录下，不在非交互 shell 的 PATH 中。

**解决**：改用直接从 git config 读取 token 的方式，或前台运行命令。

**教训**：后台任务/Bash 工具执行时需注意 PATH 环境，Homebrew 安装的工具需要完整路径或显式加载 shell 配置文件。

---

### 3. 网络不稳定导致推送失败

**问题**：`Error in the HTTP2 framing layer` 间歇性出现。

**根因**：GitHub 服务器网络问题或 HTTP/2 连接重置，重试后通常可恢复。

**解决**：等待后重试即可。

**教训**：不是所有报错都是配置问题，网络层问题重试即可解决。

---

## 四、数据库

### 1. 创建新表后未手动导入

**问题**：Git 相关接口返回 500 错误，提示表不存在。

**根因**：`schema.sql` 添加了新表但未重新导入到 MySQL。

**解决**：手动执行 `mysql -uroot -p timeweaver < schema.sql`。

**教训**：数据库 DDL 变更不会自动同步到 MySQL。开发流程应为：修改 schema.sql → 导入到数据库 → 重启后端。

---

## 五、AI 追踪脚本

### 1. Token 用量无法自动获取

**问题**：尝试自动检测 AI 工具的 token 消耗和费用，但这些信息无法通过进程外部检测获取。

**根因**：AI 工具的 token 计数和费用只有通过 API 响应才能获知，本地进程监控只能检测到进程存在和运行时长。

**解决**：放弃自动 token 检测，仅记录使用时长（duration_sec）。token 和费用作为可选手动填写字段保留。

**教训**：功能设计前需评估数据来源的可行性。进程级监控只能获取元数据（有无、时长），无法获取应用级数据（token、模型名等需 API 调用才能获取的信息）。

---

## 六、通用教训

| 类别 | 教训 |
|------|------|
| **设计先行** | 新功能应先确认 API 和数据模型再编码，避免反复修改 |
| **验证假设** | 对框架 API、第三方库行为的假设应快速验证（写小 demo），不要直接在大项目中猜测 |
| **完整重构** | 删除代码时检查所有引用，确保没有悬挂引用 |
| **环境一致性** | 使用 Maven Wrapper、`.nvmrc`、`.tool-versions` 等工具锁定开发环境版本 |
| **错误信息** | 区分真正的配置错误和网络层瞬态错误，不要过度调试后者 |
| **渐进增强** | 展示类功能应默认分页，避免数据量增长后返工 |
