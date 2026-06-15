# 查询访问优化

## 目标

降低看板统计、报名列表、作品列表、排名和通知等高频接口的重复全量读取，减少数据库 IO、Java 内存过滤和重复计算。

## 修改范围

- `backend/competition/src/main/java/com/tangqilin/competition/storage/DatabaseStore.java`
- `backend/competition/src/main/java/com/tangqilin/competition/service/PlatformService.java`
- `backend/competition/src/main/java/com/tangqilin/competition/service/impl/UserServiceImpl.java`
- `sql/init.sql`
- `docs/architecture.md`

## 核心实现

- 在 `DatabaseStore` 中新增按业务场景命名的查询、计数和存在性判断方法。
- 将看板统计、学生报名、教师报名、学生作品、教师可见作品、通知列表和排名评分的全量扫描替换为条件查询。
- 为 `registration`、`work`、`notification` 补充组合索引和兼容旧库的索引补齐脚本。
- 用户删除校验和教师列表查询改为复用数据访问层条件查询。

## 影响范围

- 对外接口路径和响应结构不变。
- 数据量增长时，高频接口减少无意义全表读取和内存过滤。
- 旧数据库首次执行 `sql/init.sql` 时会尝试补齐新增索引。

## 验证方式

- 后端执行 `.\mvnw.cmd test`，4 个测试通过。
- 前端执行 `npm run build`，生产构建通过。

## 已知限制

- 当前仍有部分管理端全量列表属于业务需要，尚未引入分页。
- 视图组装仍存在按 ID 读取关联对象的路径，后续数据量继续增长时可进一步批量预取。
