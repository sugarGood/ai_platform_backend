package com.aiplatform.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 研发客户端实体，对应 {@code client_apps} 表。
 *
 * <p>记录平台支持接入的研发客户端（如 Claude Code、Cursor、Codex 等）信息。</p>
 */
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Boolean getSupportsMcp() { return supportsMcp; }
    public void setSupportsMcp(Boolean supportsMcp) { this.supportsMcp = supportsMcp; }
    public Boolean getSupportsCustomGateway() { return supportsCustomGateway; }
    public void setSupportsCustomGateway(Boolean supportsCustomGateway) { this.supportsCustomGateway = supportsCustomGateway; }
    public String getSetupInstruction() { return setupInstruction; }
    public void setSetupInstruction(String setupInstruction) { this.setupInstruction = setupInstruction; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
