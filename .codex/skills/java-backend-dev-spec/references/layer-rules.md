# 分层规范

## Controller 层

- controller 是路由与协同层，不是业务逻辑层。
- controller 方法要短、直白、可读。
- controller 可以做：
  - 请求绑定
  - 基于 `@Valid` 的简单校验
  - 模块里已存在的鉴权注解或权限注解
  - 很轻量的入参映射
- controller 不应该做：
  - 业务规则计算
  - 持久化编排
  - 大量对象拼装
  - 本该归属 DTO 校验或 service 逻辑的临时参数清洗
- 对外接口在行为、限制或响应语义不明显时，应补充有意义的 Javadoc 或注释。
- 路由风格以目标模块现有风格为准。

## 方法参数规范

- controller、service、manager、dao/mapper 中的方法，业务参数尽量控制在 3 个以内。
- 超过 3 个时，优先封装成 request、query、command 等对象。
- `@PathVariable`、`@RequestParam`、分页参数、mapper 注解参数等框架参数，不需要为了凑规则而强行封装；以清晰度优先。

## Service 层

- service 负责业务编排与事务边界。
- 过大的 service 按职责拆分，例如 query、create、update、validator。
- 如果 controller 可以先提取出需要的数据，就不要把 `HttpServletRequest` 这类 web 对象直接传到 service。
- 重解析、传输层解码、servlet 细节不应留在 service 层。
- 相比长串基础类型参数，优先使用明确的输入模型。

## 事务规范

- `@Transactional` 要谨慎使用，不能机械加注解。
- 事务块聚焦数据库工作单元，除非必要，不要把长耗时外部调用和重逻辑混进事务里。
- 团队规范要求使用事务时优先写成 `rollbackFor = Throwable.class`。
- 同类内部方法调用不会触发 Spring 事务代理。
- 如果同一流程中确实需要事务代理，优先抽成独立 bean；只有项目本身已接受该模式且收益明确时，才考虑 `AopContext.currentProxy()`。

## DAO / Mapper 层

- 优先使用仓库现有的 MyBatis-Plus 模式。
- Mapper/DAO 方法按 SQL 语义命名：
  - `get` 用于单条查询
  - `list` 用于多条查询
  - `count` 用于聚合统计
  - `save` / `insert` 用于插入
  - `update` 用于更新
  - `remove` / `delete` 用于删除
- 避免 `getInternalData` 这类业务语义模糊的名称。
- XML 中不要硬编码本该由参数传入的业务常量。
- 尽量避免 `select *`，有条件时显式列出字段。

## 注释规范

- 非显然逻辑、对外 API、业务约束需要注释。
- 注释优先解释“为什么”，不要只是重复代码表面含义。
- 注释必须与实现保持同步。
- `TODO` / `FIXME` 应包含作者标识：

```java
// TODO <author-name>: 补充 xx 处理
// FIXME <author-name>: xx 缺陷
```
