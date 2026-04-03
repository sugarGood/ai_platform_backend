---
name: java-model-converter-rules
description: 修改 DTO、实体对象和对象转换逻辑时使用，确保对象边界清晰并遵循 DTO/PO 与 MapStruct 约定。
---

# Java 模型与转换规范

## 模型边界

- 传输对象使用 DTO，持久化对象使用 PO/Entity。
- DTO 不继承数据库对象。
- 数据库对象不直接在 Controller 层使用，必须先转换。

## 字段规范

- 基本类型字段优先使用包装类型（如 `Integer`、`Boolean`）。
- 布尔字段统一使用 `*Flag` 后缀，避免 `is*` 命名。
- 字段注释保持语义清晰并与数据库含义一致。

## Lombok 约定

建议按当前仓库风格使用必要 Lombok 注解，保持对象构造和可读性统一。

## 转换规范

- 统一通过 MapStruct 做 DTO/PO 转换。
- 按模块集中维护转换器，避免到处手写赋值代码。
- 复杂转换逻辑在转换层显式表达，不在 Controller 内拼装。

## 充血模型原则（适度）

- 允许把简单验证、状态转换放在对象行为中。
- 避免把对象堆成“纯 getter/setter 的贫血模型”。
- 同时避免过度膨胀，复杂业务仍在 Service 编排。
