# 模型与数据库规范

## 对象边界

- 保持仓库当前“传输 DTO”与“持久化 entity”之间的边界。
- 当前仓库的持久化对象主要放在 `entity/` 目录，且通常不带 `PO` 后缀；新增或修改时保持这一约定。
- 请求和响应 DTO 留在 controller 边界。
- 不要让 DTO 继承持久化实体。
- 不要直接从 controller 暴露持久化实体，除非该模块历史上就是这样且本次任务不打算重构。
- 非平凡转换优先使用 MapStruct 或其他显式映射层。

## JavaBean / 模型规范

- 当字段有空值语义时，优先使用包装类型。
- 除非确实是领域默认值，否则不要随手加字段默认值。
- 对关键业务 DTO 和 entity，尤其是持久化模型与对外请求响应对象，补充字段注释。
- 在项目已经使用 Lombok 的地方，优先延续 builder 与精简注解风格。
- 倾向于“适度充血模型”：
  - 校验、归一化、简单派生逻辑可以放在对象上
  - 编排、跨聚合规则、持久化流程仍归 service 负责

## Boolean 命名规范

- Java 字段名避免使用 `isXxx`。
- Java 侧优先使用 `xxxFlag`，数据库字段优先使用 `xxx_flag`。
- 这个规则是为了减少序列化和反射场景下的歧义。

## Schema 规范

- 新表优先包含标准审计字段：`id`、`create_time`、`update_time`，除非该区域 schema 已有不同历史约定。
- 状态字段、枚举型字段要在注释里写明所有合法取值。
- 新增或修改状态语义时，同步更新列注释。
- 使用足够小且合适的字段类型。
- 只有数据天然定长时才优先定长类型。
- 索引按需创建，命名清晰，例如 `user_name_index`。

## 仓库内补充规则

- 如果 schema 字段发生变化，顺带检查以下位置是否也要同步：
  - entity 类
  - request/response DTO
  - mapper
  - service 逻辑
  - 测试 schema 或 H2 初始化数据
- 如果新增筛选字段或 dashboard 字段，要同时检查查询对象和对外响应对象是否都已更新。
- 当前仓库新增 DTO 时，优先沿用现有命名：`CreateXxxRequest`、`UpdateXxxRequest`、`XxxResponse`、`XxxRowResponse`、`XxxFilter`。
