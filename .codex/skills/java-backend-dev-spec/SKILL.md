---
name: java-backend-dev-spec
description: 当修改或评审 Java、Spring Boot、MyBatis 后端代码时使用，包括 controller、service、mapper、DTO、entity、SQL schema 以及需要遵循团队开发规范的交付改动。
---

# Java 后端开发规范

这个 skill 基于你提供的开发规范整理，并且按当前仓库的实际情况做了兼容处理。

## 何时使用

当工作涉及以下内容时启用：

- `controller`, `service`, `mapper`, `entity`, `dto`, `common`, `resources`
- SQL schema 或 mapper XML
- Java API 设计、请求响应模型、命名、注释、事务
- 后端分层、持久化边界、提交前检查相关的代码评审

## 仓库兼容策略

原始规范在部分地方比当前仓库更严格。使用时遵循以下兼容原则：

- 不要为了满足规范而大面积重写现有代码。
- 某个模块如果已经使用 RESTful 路由，就保持原风格；只有该模块本来就是动作式路由，或用户明确要求时，才新增动作式接口。
- 当前仓库已经广泛使用 `entity`、`*Request`、`*Response` 命名，不强行回退成 `DO/DTO/PO` 全量后缀体系。
- 真正目标是边界清晰：传输对象留在边界层，持久化对象留在持久化层，转换过程保持显式。
- 复用目标模块现有的统一响应、校验和异常处理模式。

## 使用步骤

1. 先判断本次改动落在哪一层：controller、service、mapper/dao、模型、schema 或交付检查。
2. 只读取相关参考文件：
   - 分层与事务：`references/layer-rules.md`
   - 模型与数据库：`references/model-and-db-rules.md`
   - 交付前检查：`references/delivery-checklist.md`
3. 规范只增量应用到新增代码和你实际修改到的代码行。
4. 如果仓库已有约定与原始规范冲突，优先保留仓库现状，并在回复里说明原因。

## 硬性规则

- 改动后代码必须可编译，并与已有测试预期保持一致。
- 方法业务参数尽量不超过 3 个；超过时优先封装为 request、query 或 command 对象，除非框架约束让封装反而更差。
- controller 必须保持薄层，只做参数接收、简单校验和分发。
- service 负责业务编排；能在 controller 中归一化的 web 层对象不要继续向内传。
- 事务边界必须明确，避免误用同类内部调用导致事务失效。
- mapper/dao 方法命名必须体现查询语义，XML 中不要隐藏硬编码业务常量。
- 持久化对象不要直接穿透到 controller 层，除非该模块原本就这样且本次任务不包含重构。
- 布尔字段和布尔列优先使用 `*Flag`，不要使用 `is*` 作为字段名。
- 注释要解释意图、约束和非显然逻辑，并且必须与代码同步。

## 启用后的工作方式

当该 skill 生效时：

- 如果规范冲突会实质影响改动，先指出冲突点再动手。
- 变更范围只围绕用户任务，不做顺手大重构。
- 避免推测性重写。
- 如果用户问原因或要 review，明确说明用了哪些规则。
