# 高校竞赛管理系统

高校竞赛管理系统用于支撑学校竞赛的前台展示、学生报名、作品提交、教师审核评审、管理员维护与通知管理。项目采用前后端分离架构，前端基于 Vue 3，后端基于 Spring Boot 3，数据存储使用 MySQL。

## 功能概览

- 前台首页：竞赛展示、报名入口、登录入口、站点底部信息栏。
- 账号认证：登录、学生注册、JWT 登录态、角色路由控制。
- 学生端：查看竞赛、提交报名、上传作品、查看通知、维护个人资料、智能答疑。
- 教师端：报名审核、作品评审、通知查看、个人资料维护。
- 管理端：用户管理、竞赛管理、报名总览、作品分配、成绩排名、通知管理。
- 文件上传：报名材料和作品文件上传，默认保存到后端 `uploads` 目录。

## 技术栈

### 前端

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios

### 后端

- Java 17
- Spring Boot 3.2.6
- MyBatis-Plus 3.5.10
- MySQL
- Spring Security Crypto
- JJWT
- Hutool

## 项目结构

```text
competition-management-system/
├─ backend/competition/        # Spring Boot 后端项目
├─ frontend/                   # Vue 3 前端项目
├─ sql/init.sql                # 数据库初始化脚本
├─ docs/                       # 需求、接口、数据库和架构文档
├─ features/                   # 功能实现与变更记录
└─ README.md
```

## 环境要求

- JDK 17+
- Maven 3.8+，也可以使用后端自带的 `mvnw.cmd`
- Node.js 18+
- npm 或 pnpm
- MySQL 8.x

## 快速启动

### 1. 初始化数据库

在 MySQL 中执行初始化脚本：

```sql
source sql/init.sql;
```

如果使用图形化数据库工具，也可以直接打开 [sql/init.sql](sql/init.sql) 并执行全部 SQL。

默认数据库名为 `competition_db`。后端默认连接配置：

```yaml
url: jdbc:mysql://localhost:3306/competition_db
username: root
password: 123456
```

如本机配置不同，可以通过环境变量覆盖：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/competition_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的密码"
```

### 2. 启动后端

```powershell
cd backend\competition
.\mvnw.cmd spring-boot:run
```

后端默认端口：

```text
http://localhost:8080
```

### 3. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

前端默认端口：

```text
http://localhost:5173
```

如果 `5173` 被占用，Vite 会自动切换到下一个可用端口。前端开发服务会将 `/api` 代理到 `http://localhost:8080`。

## 默认账号

初始化脚本会创建默认管理员账号：

```text
账号：admin
密码：admin123
角色：管理员
```

请以实际数据库初始化结果为准。

## 常用命令

### 前端

```powershell
cd frontend
npm run dev       # 启动开发服务
npm run build     # 类型检查并构建生产包
npm run preview   # 预览生产构建
```

### 后端

```powershell
cd backend\competition
.\mvnw.cmd spring-boot:run   # 启动后端
.\mvnw.cmd test              # 运行测试
.\mvnw.cmd package           # 打包
```

## 关键配置

后端配置文件：

- [backend/competition/src/main/resources/application.yml](backend/competition/src/main/resources/application.yml)

常用环境变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/competition_db...` | MySQL 连接地址 |
| `DB_USERNAME` | `root` | 数据库用户名 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `JWT_SECRET` | `your-secret-key-32-characters-long-123456` | JWT 签名密钥 |
| `JWT_EXPIRATION` | `86400000` | Token 过期时间，单位毫秒 |
| `APP_UPLOAD_DIR` | `uploads` | 文件上传目录 |
| `APP_CORS_ALLOWED_ORIGINS` | 本地开发地址列表 | 允许跨域访问的前端地址 |

生产环境请务必设置独立的 `JWT_SECRET`，不要直接使用默认值。

## 主要页面

| 路径 | 说明 |
| --- | --- |
| `/` | 前台首页 |
| `/login` | 登录页 |
| `/register` | 学生注册页 |
| `/student/dashboard` | 学生工作台 |
| `/teacher/dashboard` | 教师工作台 |
| `/admin/dashboard` | 管理员工作台 |

路由会根据登录状态和用户角色进行访问控制。

## 项目文档

- [docs/需求文档.md](docs/需求文档.md)
- [docs/接口文档.md](docs/接口文档.md)
- [docs/数据库设计.md](docs/数据库设计.md)
- [docs/architecture.md](docs/architecture.md)
- [features/](features/)

## 开发约定

- 前端新增页面时同步维护路由和角色访问规则。
- 后端接口统一返回结果对象，异常由全局异常处理器处理。
- 新增或调整核心业务流程时，同步更新 `docs/architecture.md`。
- 每次独立功能、修复或优化完成后，在 `features/` 中补充对应说明。
- 不要提交密钥、Token、临时日志、构建产物或本机私有配置。

## 常见问题

### 前端请求接口失败

确认后端已启动在 `http://localhost:8080`，并检查前端开发服务是否通过 Vite 代理访问 `/api`。

### 数据库连接失败

确认 MySQL 已启动，`competition_db` 已初始化，且 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 与本机数据库一致。

### 注册后没有直接进入系统

当前设计为注册成功后返回登录页，用户需要重新登录。

### 上传文件失败

检查 `APP_UPLOAD_DIR` 指向的目录是否存在、后端进程是否有写入权限，以及上传文件是否超过配置限制。
