package com.aiplatform.backend.common.exception;

/**
 * 稳定业务错误码（大写下划线），与 HTTP 状态相互独立，供前端与监控识别。
 *
 * <p>新增异常时请在此登记常量，避免魔法字符串。</p>
 */
public final class BizErrorCode {

    private BizErrorCode() {}

    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";

    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    /** 邮箱、用户名等唯一约束冲突 */
    public static final String CONFLICT = "CONFLICT";
    public static final String DEPARTMENT_NOT_FOUND = "DEPARTMENT_NOT_FOUND";
    public static final String PROJECT_NOT_FOUND = "PROJECT_NOT_FOUND";
    public static final String PROJECT_MEMBER_NOT_FOUND = "PROJECT_MEMBER_NOT_FOUND";
    public static final String PROJECT_MEMBER_ALREADY_EXISTS = "PROJECT_MEMBER_ALREADY_EXISTS";
    public static final String PROJECT_AGENT_NOT_FOUND = "PROJECT_AGENT_NOT_FOUND";

    public static final String KNOWLEDGE_BASE_NOT_FOUND = "KNOWLEDGE_BASE_NOT_FOUND";
    /** 项目下指定的全局知识库绑定配置不存在 */
    public static final String PROJECT_KNOWLEDGE_CONFIG_NOT_FOUND = "PROJECT_KNOWLEDGE_CONFIG_NOT_FOUND";
    public static final String KB_DOCUMENT_NOT_FOUND = "KB_DOCUMENT_NOT_FOUND";
    public static final String INGEST_IN_PROGRESS = "INGEST_IN_PROGRESS";
    public static final String NO_OBJECT_KEY = "NO_OBJECT_KEY";
    public static final String INGEST_DISABLED = "INGEST_DISABLED";
    public static final String SKILL_NOT_FOUND = "SKILL_NOT_FOUND";
    public static final String AI_MODEL_NOT_FOUND = "AI_MODEL_NOT_FOUND";
    public static final String AI_PROVIDER_NOT_FOUND = "AI_PROVIDER_NOT_FOUND";
    public static final String PROVIDER_API_KEY_NOT_FOUND = "PROVIDER_API_KEY_NOT_FOUND";
    public static final String PROVIDER_FAILOVER_POLICY_NOT_FOUND = "PROVIDER_FAILOVER_POLICY_NOT_FOUND";
    public static final String TOOL_DEFINITION_NOT_FOUND = "TOOL_DEFINITION_NOT_FOUND";
    public static final String MCP_SERVER_NOT_FOUND = "MCP_SERVER_NOT_FOUND";
    public static final String ROLE_NOT_FOUND = "ROLE_NOT_FOUND";
    public static final String PERMISSION_NOT_FOUND = "PERMISSION_NOT_FOUND";
    public static final String PLATFORM_CREDENTIAL_NOT_FOUND = "PLATFORM_CREDENTIAL_NOT_FOUND";
    public static final String CLIENT_APP_NOT_FOUND = "CLIENT_APP_NOT_FOUND";
}
