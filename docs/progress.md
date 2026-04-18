# Sakura AI Reviewer Android - 开发进度

> 最后更新：2026-04-18

## 项目概况

- **应用类型**：原生 Android（Kotlin + Jetpack Compose + Material3）
- **后端 API**：`https://pr-bot.firefly520.top/api/v1/`（66 个端点）
- **架构模式**：MVVM + Repository + 单模块分层
- **技术栈**：Compose + Hilt + Retrofit/OkHttp/Moshi + Navigation Compose + DataStore
- **minSdk**：31 | **targetSdk**：36
- **源码文件数**：58 个 Kotlin 文件

---

## 已完成阶段

### Phase 1: 基础设施

| 文件 | 说明 |
|------|------|
| `core/network/ApiResponse.kt` | 统一响应包装 `{success, message, data}` + `EmptyData` |
| `core/network/ApiResult.kt` | UI 状态 sealed class + `toUserMessage()` 错误提取 |
| `core/network/NetworkModule.kt` | Hilt @Module：OkHttp、Retrofit、Moshi、全部 ApiService |
| `core/network/AuthInterceptor.kt` | Bearer Token 注入 + 请求/响应日志 |
| `core/network/TokenExpiryInterceptor.kt` | 本地 Token 过期检测 |
| `core/network/UnauthorizedInterceptor.kt` | 401 响应日志 |
| `core/security/TokenManager.kt` | EncryptedSharedPreferences 存储 JWT |
| `core/security/SecurityModule.kt` | Hilt 安全模块 |
| `core/auth/Role.kt` | `enum Role { USER, ADMIN, SUPER_ADMIN }` + 权限判断 |
| `core/auth/AuthState.kt` | `sealed AuthState { Unauthenticated, SetupRequired, Authenticated }` |
| `core/auth/SessionManager.kt` | 全局 AuthState Flow + 启动验证 |
| `core/ui/theme/` | Material3 主题（Color/Type/Theme） |
| `SakuraApplication.kt` | @HiltAndroidApp |
| `MainActivity.kt` | 单 Activity 宿主 |

### Phase 2: 认证与导航

| 文件 | 说明 |
|------|------|
| `feature/auth/data/AuthApiService.kt` | OAuth 登录/回调/登出/Me 接口 |
| `feature/auth/domain/model/` | TokenInfo、User 模型 |
| `feature/auth/ui/LoginScreen.kt` | WebView 内嵌 GitHub OAuth 登录 |
| `feature/auth/ui/AuthViewModel.kt` | 登录状态管理 |
| `core/navigation/NavRoutes.kt` | 全部路由定义（sealed class） |
| `core/navigation/AppNavigation.kt` | NavHost + 角色感知底部导航（Dashboard/Reviews/Scans/Admin） |
| `core/navigation/RoleBasedNavGuard.kt` | Composable 角色守卫 |

### Phase 3: 核心功能

| 模块 | API Service | ViewModel | Screen | 状态 |
|------|------------|-----------|--------|------|
| Dashboard | `DashboardApiService` (stats/recent/chart/refresh) | `DashboardViewModel` | `DashboardScreen` | 已测试通过 |
| Review 列表/详情 | `ReviewApiService` (list/detail/files/comments) | `ReviewListViewModel` / `ReviewDetailViewModel` | `ReviewListScreen` / `ReviewDetailScreen` | 已测试通过 |
| Issue 列表/详情 | `IssueApiService` (list/stats/detail/reanalyze) | `IssueListViewModel` / `IssueDetailViewModel` | `IssueListScreen` / `IssueDetailScreen` | 已测试通过 |
| Settings | `SettingsApiService` (get/update/about) | `SettingsViewModel` | `SettingsScreen` | 已测试通过 |

### Phase 4: 管理功能

| 模块 | API Service | ViewModel | Screen | 状态 |
|------|------------|-----------|--------|------|
| 用户管理 | `UserApiService` (CRUD/role/quota/toggle/reset) | `UserListViewModel` / `UserDetailViewModel` | `UserListScreen` / `UserDetailScreen` | 已测试通过 |
| 仓库管理 | `RepoApiService` (list/index-docs/index-code/index-issues/scan) | `RepoListViewModel` | `RepoListScreen` | 已构建 |
| 队列监控 | `QueueApiService` (stats/items/retry/delete/purge) | `QueueViewModel` | `QueueScreen` | 已构建 |
| 审查日志 | `LogApiService` (review-logs/action-logs) | `ReviewLogListViewModel` / `ReviewLogDetailViewModel` / `ActionLogListViewModel` | `ReviewLogListScreen` / `ReviewLogDetailScreen` / `ActionLogListScreen` | 已构建 |
| 操作日志 | 同上 `LogApiService` | `ActionLogListViewModel` | `ActionLogListScreen` | 已构建 |

---

## 已解决的关键问题

| 问题 | 原因 | 修复 |
|------|------|------|
| ReviewDetail 导航崩溃 ClassCastException | `savedStateHandle["reviewId"]` 路径参数为 String，不能直接强转 Int | 改用 `savedStateHandle.get<String>("reviewId")?.toIntOrNull()` |
| pr_id JSON 解析失败 | GitHub PR ID 超过 `Int.MAX_VALUE`（如 3545705466） | `Int` → `Long`（ReviewItemData、ReviewDetailData、RecentReviewData） |
| telegram_id JSON 解析失败 | Telegram ID 超过 `Int.MAX_VALUE`（如 5553479213） | `Int` → `Long`（UserItemData、UserDetailData、CreateUserRequest、UpdateUserInfoRequest） |
| Moshi 无法序列化 `Unit` | `ApiResponse<Unit>` 中 Moshi 不支持 Kotlin `Unit` | 创建 `EmptyData` 空类替代，全局替换 16 处 |
| 服务端错误信息不显示 | `HttpException` 的 `message()` 返回 "HTTP 400"，非中文错误 | 实现 `toUserMessage()` 从 error body 提取服务端错误信息，30 处 catch 块替换 |
| `toUserMessage()` 解析失败 | error Moshi 缺少 `KotlinJsonAdapterFactory` | 添加 `KotlinJsonAdapterFactory` + Regex fallback |

---

## 导航结构

### 底部导航 Tab

| Tab | 路由 | 可见角色 |
|-----|------|---------|
| Dashboard | `dashboard` | USER / ADMIN / SUPER_ADMIN |
| Reviews | `reviews` | USER / ADMIN / SUPER_ADMIN |
| Scans | `scans` | USER / ADMIN / SUPER_ADMIN |
| Admin | `admin/users` | ADMIN / SUPER_ADMIN |

### Admin 子页面（从 Admin Tab 或 Settings 进入）

| 页面 | 路由 | 说明 |
|------|------|------|
| UserList | `admin/users` | 用户列表（搜索/角色过滤/分页） |
| UserDetail | `admin/users/{userId}` | 用户详情 + 角色/配额/启用管理 |
| RepoList | `admin/repos` | 仓库列表 + 索引触发 |
| QueueMonitor | `admin/queue` | 队列统计 + 列表（重试/删除/清理） |
| ActionLogs | `admin/logs/actions` | 操作日志列表 |

### 其他页面

| 页面 | 路由 | 说明 |
|------|------|------|
| ReviewDetail | `reviews/{reviewId}` | 审查详情（Summary + Files + Comments） |
| IssueDetail | `issues/{issueId}` | Issue 详情 + 重新分析 |
| ReviewLogs | `logs/reviews` | 审查日志列表 |
| ReviewLogDetail | `logs/reviews/{reviewId}` | 审查日志详情 |
| Settings | `settings` | 主题/每页数量/关于/登出 |

---

## 待完成阶段

### Phase 5: 高级功能

| 模块 | 涉及 API | 说明 |
|------|---------|------|
| **SSE 实时事件** | `GET /events` | OkHttp EventSource + Flow + 自动重连（事件格式已更新为 `event: <类型>` + 30s keepalive） |
| **扫描管理** | `GET/POST /scans` (list/stats/detail/trigger/retry/cancel) | ScanListScreen + ScanDetailScreen |
| **系统配置** | `GET/PATCH /config/general` + strategies + labels | ConfigScreen（多 Tab，super_admin 专属） |
| **Setup Wizard** | `GET /setup/state` + save-step + complete | 初始化引导（免认证） |

### Phase 6: 打磨完善

| 任务 | 说明 |
|------|------|
| Room 离线缓存 | DashboardStats / Reviews / Issues / Scans 缓存（网络优先策略） |
| 下拉刷新 | SwipeRefresh 组件 |
| CSV 导出 | `GET /reviews/export` 下载 |
| Dashboard stats 500 错误 | 后端 `Decimal is not JSON serializable`，需后端修复（avg_score 字段） |
| 错误处理统一 | 其他 Screen 的错误提示也改为 errorContainer 卡片样式 |
| ProGuard | Release 混淆规则 |
| 测试 | 单元测试 + UI 测试 |

---

## API 端点覆盖情况

| API 分组 | 端点数 | 已接入 | 待接入 |
|---------|--------|--------|--------|
| Auth (3.2) | 5 | 5 | 0 |
| Setup (3.3) | 4 | 0 | 4 |
| Dashboard (3.4) | 4 | 4 | 0 |
| Reviews (3.5) | 6 | 6 | 0 |
| Issues (3.6) | 4 | 4 | 0 |
| Users (3.7) | 10 | 10 | 0 |
| Repos (3.8) | 5 | 5 | 0 |
| Config (3.9) | 6 | 0 | 6 |
| Logs (3.10) | 4 | 4 | 0 |
| Queue (3.11) | 6 | 6 | 0 |
| Scans (3.12) | 6 | 0 | 6 |
| Settings (3.13) | 3 | 3 | 0 |
| Events/SSE (3.14) | 1 | 0 | 1 |
| **合计** | **66** | **47** | **19** |

---

## 注意事项

1. **Int 溢出**：所有来自 GitHub/Telegram 的 ID 字段必须使用 `Long`（`pr_id`、`telegram_id`）
2. **SavedStateHandle**：Navigation 路径参数为 String，需 `.get<String>().toIntOrNull()`
3. **Moshi + Unit**：Retrofit 返回类型不可使用 `Unit`，必须用 `EmptyData`
4. **SSE 格式变更**：后端已从 `event: message` 改为 `event: <具体类型>` + 30s keepalive
5. **Auth 限流**：OAuth 端点新增 10次/分钟限流
