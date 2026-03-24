package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 研发客户端实体，对应 {@code client_apps} 表。
 *
 * <p>记录平台支持接入的研发客户端（如 Claude Code、Cursor、Codex 等）信息。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("client_apps")
public class ClientApp {

    /** 主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 客户端编码（如 claude_code / cursor） */
    private String code;
    /** 客户端名称 */
    private String name;
    /** 图标 */
    private String icon;
    /** 是否支持 MCP 协议 */
    private Boolean supportsMcp;
    /** 是否支持自定义网关 */
    private Boolean supportsCustomGateway;
    /** 接入指南模板（Markdown） */
    private String setupInstruction;
    /** 状态：ACTIVE / DISABLED */
    private String status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 最后更新时间 */
    private LocalDateTime updatedAt;

}
