# 云农智诊（YunNongZhiZhen）

农作物病虫害识别与诊断平台 Web 版 —《软件工程及 UML》课程设计。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3、Spring Security、JWT、MyBatis-Plus、MySQL 8 |
| 前端 | Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios |
| AI   | DeepSeek 视觉/文本模型（OpenAI 兼容，仅后端调用） |

## 目录结构

```
├── backend/     # 根包 com.yunnongzhizhen
├── frontend/    # 项目名 yun-nong-zhi-zhen
├── sql/         # 建库与建表脚本
└── docs/        # 权限矩阵、测试用例、UML 对照、运行说明
```

## 快速开始

### 数据库

```bash
mysql -u root -p < sql/01_create_database.sql
mysql -u root -p yunnongzhizhen < sql/02_schema.sql
mysql -u root -p yunnongzhizhen < sql/03_seed.sql
```

### 后端

```bash
cd backend
# 配置 DB_*/JWT_SECRET/DEEPSEEK_API_KEY 或 application-dev.yml
mvn spring-boot:run
```

默认管理员：`admin` / `Admin@123`

### 前端

```bash
cd frontend
npm install
npm run dev
```

### 集成测试

```bash
cd backend
mvn test
```

## 文档

| 文档 | 说明 |
|------|------|
| [docs/测试环境与运行说明.md](docs/测试环境与运行说明.md) | 环境、命令、测试摘要 |
| [docs/测试用例表.md](docs/测试用例表.md) | 用例编号与执行结果 |
| [docs/代码-UML对照表.md](docs/代码-UML对照表.md) | 代码 ↔ 类图/用例/顺序图/状态图 |
| [docs/权限矩阵.md](docs/权限矩阵.md) | 功能 × FARMER/EXPERT/ADMIN |

## 敏感配置（环境变量）

- `DB_USERNAME` / `DB_PASSWORD`
- `JWT_SECRET` / `JWT_EXPIRATION_MS`
- `DEEPSEEK_API_KEY` / `DEEPSEEK_BASE_URL`
- `UPLOAD_BASE_PATH`
