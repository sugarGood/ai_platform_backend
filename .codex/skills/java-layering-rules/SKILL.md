---
name: java-layering-rules
description: 修改 Java 后端 controller、service 时使用，确保分层边界、参数控制、事务使用符合团队规范。
---

# Java 分层与事务规范

## Controller 规范

- Controller 只做参数接收、简单 `@Valid` 校验与服务分发。
- 不在 Controller 中写业务编排、复杂数据拼装。
- 新增接口建议补充方法注释（说明参数、行为、返回）。

## 方法参数规范

- 一个方法参数尽量不超过 3 个。
- 超过 3 个时优先封装为 request/query/command 对象。

## Service 规范

- Service 负责业务编排，避免把 `HttpServletRequest` 等 Web 对象下沉到 Service。
- 大业务按职责拆分服务，例如 Query/Create/Validator 等。

## 事务规范

- 谨慎使用 `@Transactional(rollbackFor = Throwable.class)`。
- 控制事务范围，避免在事务内执行耗时非 DB 逻辑。
- 注意同类内部方法调用不触发事务代理，避免事务失效。

## 命名与注释

- 方法名尽量自解释，注释用于说明意图、约束、边界条件。
- 修改代码时必须同步更新相关注释，避免注释与实现不一致。
