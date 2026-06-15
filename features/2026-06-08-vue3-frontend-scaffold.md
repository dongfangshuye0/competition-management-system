# Vue3 前端工程补齐

## 目标

根据新版需求文档补齐 `frontend/` 前端工程，使项目具备独立 Vue3 + Vite 开发入口，并能通过后端 RESTful API 串起学生、教师和管理员的主要流程。

## 修改范围

- `frontend/package.json`
- `frontend/vite.config.ts`
- `frontend/tsconfig.json`
- `frontend/src/`
- 根目录 `.gitignore`

## 核心实现

- 新增 Vue3、Vue Router、Pinia、Axios、Element Plus 前端工程配置。
- 新增 Axios 实例，统一注入 JWT Token，并在 401 时清理登录态。
- 新增角色路由守卫，按 `student`、`teacher`、`admin` 隔离页面访问。
- 新增 `/` 公共首页，展示系统入口、公开竞赛和基础统计，登录后可直接进入对应角色工作台。
- 补齐登录、工作台、竞赛、报名、作品、通知、用户、个人资料和智能答疑页面。
- 使用 Vite 代理 `/api` 到 `http://localhost:8080`，保持前后端开发模式分离。
- 忽略 `frontend/node_modules/` 和 `frontend/dist/`，避免提交依赖目录和构建产物。

## 影响范围

- 前端可通过 `npm run dev` 在 5173 端口独立启动。
- 生产构建产物输出到 `frontend/dist/`；后端不再内嵌静态前端页面，只保留 RESTful API 服务。
- 当前页面以流程闭环为主，视觉设计保持克制，后续可继续做细粒度交互优化。

## 验证方式

- 在 `frontend` 执行 `npm install` 安装依赖。
- 在 `frontend` 执行 `npm run build`，类型检查和 Vite 构建通过。
- 在 `backend/competition` 执行 `.\mvnw.cmd test`，后端 4 个测试通过。
- 访问 `http://localhost:5173` 可进入公共首页，`/api` 请求由前端服务代理到后端。

## 已知限制

- 首次使用前端需先安装依赖，本机未安装 `pnpm` 时使用 `npm install`。
- 文件上传控件当前保留为表单字段骨架，后续可补充 Element Plus Upload 组件体验。
- Vite 构建提示主包较大，后续可按路由做动态导入拆包。
