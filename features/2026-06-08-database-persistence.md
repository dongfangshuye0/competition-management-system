# 数据库持久化接入

## 目标

将用户、竞赛、报名、作品、评分和通知从内存演示存储切换到 MySQL 数据库持久化保存。

## 修改范围

- Spring Boot 数据源配置
- MyBatis-Plus 自动配置和 Mapper 扫描
- 实体表名、主键、逻辑删除和特殊字段映射
- 竞赛、报名、作品、评分、通知 Mapper
- `DatabaseStore` 数据访问封装
- MySQL 初始化脚本和架构文档

## 核心实现

- 打开 `DataSourceAutoConfiguration` 和 MyBatis-Plus 自动配置。
- 在 `application.yml` 中配置 MySQL，支持通过 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 覆盖。
- 新增各业务实体的 `BaseMapper`，让现有服务层通过数据库读写。
- 将旧的内存演示存储命名调整为 `DatabaseStore`，由它统一封装 Mapper 持久化访问，减少 controller/service 对底层数据访问的感知。
- 首次启动时自动补齐演示用户、竞赛、报名、作品、评分和通知数据。
- 修正数据库字段：用户班级统一为 `class_name`，通知已读状态映射为 `is_read`，作品表补充评审教师和 AI 初评字段。

## 影响范围

- 应用启动依赖 MySQL 服务和 `competition_db` 数据库连接。
- 删除操作使用 MyBatis-Plus 逻辑删除，数据不会直接物理移除。
- 重启应用后业务数据不再丢失。

## 验证方式

- 执行 `sql/init.sql` 初始化/升级本机 MySQL。
- 在 `backend/competition` 执行 `.\mvnw.cmd test`，4 个测试全部通过。
- 启动服务后调用 `/api/auth/login` 和 `/api/dashboard`，返回用户 5、竞赛 3、报名 3、作品 2。
- 直接查询 MySQL，`user`、`competition`、`registration`、`work` 的有效记录数分别为 5、3、3、2。

## 已知限制

- 数据库初始化仍需手动执行 `sql/init.sql`。
- 当前默认数据库密码为本地开发值，部署时必须通过环境变量覆盖。
- 文件上传内容仍保存在本地目录，数据库只保存文件访问路径。
