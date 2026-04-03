---
name: java-dao-sql-rules
description: 修改 mapper、dao、MyBatis XML 或 SQL 查询时使用，确保命名、参数传递、SQL 语义与复用性符合团队规范。
---

# DAO / Mapper / SQL 规范

## 框架与基本约定

- 优先使用 MyBatis-Plus。
- DAO/Mapper 接口按仓库既有模式维护。

## SQL 编写

- 不在 XML 中硬编码业务常量，常量由 DAO 参数传入。
- 避免 `select *`，显式列出需要字段。
- SQL 命名和查询语义保持一致，避免业务耦合命名。

## DAO 方法命名

- 单个查询：`get*`
- 列表查询：`list*`
- 统计查询：`count*`
- 新增：`save*` / `insert*`
- 删除：`remove*` / `delete*`
- 更新：`update*`

## 参数规范

- Mapper 方法参数需明确命名（如 `@Param`），便于 XML 可读。
- 参数超 3 个时优先封装参数对象。

## 评审检查

- 是否存在硬编码常量。
- 是否存在星号查询。
- 方法名是否体现 SQL 行为。
- 新增 SQL 是否具备基础索引可用性。
