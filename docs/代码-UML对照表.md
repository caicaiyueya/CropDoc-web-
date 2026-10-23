# 云农智诊 — 代码与 UML 对照表（阶段10）

| 核心类/接口 | 类图类 | 用例 | 顺序图消息 | 状态图迁移 |
|-------------|--------|------|------------|------------|
| `YunNongZhiZhenApplication` | 系统根 / 部署节点 | — | main→启动 | — |
| `AuthController` | 边界类 AuthBoundary | 注册、登录、改密、重置密码 | register / login / changePassword / resetPassword | — |
| `AuthService` | 控制类 AuthControl | 同上 | encode→insert；matches→generateToken | User.status：NORMAL 可登录 / DISABLED 拒绝 |
| `JwtUtil` | 安全辅助类 | 登录签发 Token | generateToken / parseUserId | — |
| `JwtAuthenticationFilter` | 安全控制类 | 权限校验（每次请求） | parseUserId→selectById→SecurityContext | 以 DB 最新 role 生效 |
| `LoginUser` | 会话主体 | — | getAuthorities（ROLE_*） | — |
| `SecurityConfig` | 配置组件 | 路径授权 | filterChain | — |
| `User` / `UserMapper` | 实体 User | 注册/资料/角色管理 | insert / selectById / updateById | role：FARMER↔EXPERT↔ADMIN；status：NORMAL↔DISABLED |
| `UserRole` / `UserStatus` | 枚举 | RBAC | — | 同上 |
| `UserCertificationController` | 边界类（用户端认证） | 提交认证、查状态 | submit / my / status / expertPing | — |
| `AdminCertificationController` | 边界类（管理端认证） | 审核通过/驳回 | list / approve / reject / logs | — |
| `CertificationService` | 控制类 CertificationControl | 提交认证、审核通过、审核驳回 | submitCertification；approveCertification；rejectCertification | CertStatus：PENDING→APPROVED / REJECTED；通过时 User.role→EXPERT |
| `CertificationApplication` | 实体 CertificationApplication | 认证申请 | insert / updateById | PENDING→APPROVED/REJECTED |
| `UploadController` / `FileStorageService` | 边界/控制（上传） | 上传图片 | storeWithMeta | — |
| `DetectionController` | 边界类 DetectionBoundary | 病虫害识别、识别历史 | detect / history | — |
| `DetectionService` | 控制类 DetectionControl | detectDisease | detectByImage→insert DetectionRecord | — |
| `DiagnosisController` / `DiagnosisService` | 边界/控制 Diagnosis | 智能诊断对话 | chatDiagnose→持久化 DiagnosisSession | 会话多轮（同一 sessionId） |
| `AiService` | 外部系统适配器 | 识别、诊断 | detectByImage；chatDiagnose | — |
| `DetectionRecord` / `DiagnosisSession` | 实体 | 识别/诊断 | insert / update | — |
| `PostController` | 边界类 CommunityBoundary | 发帖、评论、收藏、举报、优质 | createPost；createComment；markQuality；toggleFavorite；createReport | — |
| `PostService` | 控制类 PostControl | 社区业务 | createPost；listPosts；getPost；createComment；markQuality | — |
| `PostStatusService` | 控制类（状态机） | 审核帖子 | approve / reject / offline | PostStatus：PENDING→APPROVED/REJECTED；APPROVED→OFFLINE |
| `Post` / `Comment` / `Favorite` / `Report` | 实体 | 社区 | CRUD | Post 状态同上；Comment.isQuality：false→true |
| `AdminPostController` | 边界类（社区审核） | 帖子审核、警告用户 | pending；approve；reject；offline；warn | 同上 |
| `FavoriteService` / `ReportService` | 控制类 | 收藏、举报、警告/禁用 | toggleFavorite；createReport；warnUser | User.status→DISABLED（禁用） |
| `UserContentController` | 边界类（内容/个人中心） | 资讯、病害、作物、资料、反馈 | news/disease/crop/profile/feedback | — |
| `NewsService` | 控制类 | 资讯浏览与维护 | listOnline；setStatus | PublishStatus：ONLINE↔OFFLINE |
| `DiseaseService` | 控制类 | 病害库（BASIC/PRO） | listByRole；getById；**canAccessPro** | — |
| `ProfileService` / `FeedbackService` | 控制类 | 个人中心、反馈 | get/updateProfile；submit；reply | FeedbackStatus：OPEN→REPLIED |
| `ContentManageService` | 控制类 | Banner、公告 | saveBanner；saveNotice | ONLINE/OFFLINE |
| `AdminManageController` | 边界类（管理后台） | 用户管理、内容维护、仪表盘 | user/list；reset-password；role；disable/enable；stats | User.role/status 变更 |
| `AdminUserService` | 控制类 | 调角色、重置密码、启禁用 | changeRole；resetPassword；disable；enable | role/status 迁移；写 AdminOperationLog |
| `DashboardService` | 控制类 | 数据概览 | stats | — |
| `AdminLogService` / `AdminOperationLog` | 实体/服务 | 操作审计 | log；listCertificationLogs | — |
| `GlobalExceptionHandler` / `ApiResult` | 基础设施 | 统一响应 | handleBusiness / 401 / 403 | — |
| 前端 `request.js` | 通信组件 | 全站 API | Bearer；401→logout | — |
| 前端 `store/user.js` | 会话对象 | 登录态 | setSession；hasRole | — |
| 前端 `router/index.js` | 导航/守卫 | 路由权限 | beforeEach | — |
| `UserLayout` / `AdminLayout` | 界面边界 | 动态菜单 | FARMER→提交认证；EXPERT→专业回答 | — |
| `CertificationApply.vue` | 界面 | 提交认证 | submitCertification | PENDING |
| `DetectUpload.vue` | 界面 | 识别上传 | upload→detect | — |
| `CertificationReview.vue` | 界面 | 认证审核 | approve / reject | PENDING→APPROVED/REJECTED |

## 分层与包结构（对应分析类图包）

| 包 | UML 层 |
|----|--------|
| `controller` | 边界类（Boundary） |
| `service` | 控制类（Control） |
| `entity` / `mapper` | 实体类（Entity）与持久化 |
| `dto` | 传输对象 |
| `security` / `config` | 安全与配置组件 |
