# First-Batch Database Schema Evolution Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Land the first batch of database changes needed by the new prototype, including schema evolution SQL, H2 test schema updates, and the minimum Java entity/service fixes required to keep persistence consistent.

**Architecture:** Keep the existing main tables and API naming, evolve the schema in place, and add new relation/version tables where the prototype requires approval, versioning, and resource-scoped authorization. Implement in small steps: fix current inconsistencies first, then add migration SQL, then sync H2 and entity mappings, and finally verify through targeted tests.

**Tech Stack:** Java 17, Spring Boot, MyBatis-Plus, MySQL 8 SQL, H2 test schema, JUnit 5, Mockito

---

### Task 1: Fix Current Persistence Inconsistencies

**Files:**
- Create: `ai-platform-server/src/test/java/com/aiplatform/backend/service/ToolDefinitionServiceTest.java`
- Modify: `ai-platform-server/src/main/java/com/aiplatform/backend/service/ToolDefinitionService.java`
- Modify: `ai-platform-server/src/main/java/com/aiplatform/backend/entity/ProjectMcpIntegration.java`

- [ ] **Step 1: Write the failing service test for tool disable status**

```java
package com.aiplatform.backend.service;

import com.aiplatform.backend.entity.ToolDefinition;
import com.aiplatform.backend.mapper.ProjectToolMapper;
import com.aiplatform.backend.mapper.ToolDefinitionMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolDefinitionServiceTest {

    @Test
    void disableShouldPersistDisabledStatus() {
        ToolDefinitionMapper toolDefinitionMapper = mock(ToolDefinitionMapper.class);
        ProjectToolMapper projectToolMapper = mock(ProjectToolMapper.class);
        ToolDefinitionService service = new ToolDefinitionService(toolDefinitionMapper, projectToolMapper);

        ToolDefinition existing = ToolDefinition.builder()
                .id(9L)
                .toolName("search_kb")
                .status("ACTIVE")
                .build();
        when(toolDefinitionMapper.selectById(9L)).thenReturn(existing);
        when(toolDefinitionMapper.updateById(any(ToolDefinition.class))).thenAnswer(invocation -> 1);

        ToolDefinition updated = service.disable(9L);

        assertEquals("DISABLED", updated.getStatus());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -pl ai-platform-server -Dtest=ToolDefinitionServiceTest test`

Expected: FAIL because `ToolDefinitionService.disable()` currently writes `INACTIVE` while the schema uses `ACTIVE/DISABLED`.

- [ ] **Step 3: Change the service to use the schema enum**

```java
/** 禁用工具（status -> DISABLED）。 */
public ToolDefinition disable(Long id) {
    ToolDefinition tool = getByIdOrThrow(id);
    tool.setStatus("DISABLED");
    toolDefinitionMapper.updateById(tool);
    return tool;
}
```

- [ ] **Step 4: Expand `ProjectMcpIntegration` to match the existing SQL columns**

```java
@TableName("project_mcp_integrations")
public class ProjectMcpIntegration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long mcpServerId;
    private String customConfig;
    private String permissionScope;
    private String status;
    private LocalDateTime connectedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 5: Re-run the focused test**

Run: `mvn -pl ai-platform-server -Dtest=ToolDefinitionServiceTest test`

Expected: PASS with `Tests run: 1, Failures: 0, Errors: 0`.

- [ ] **Step 6: Commit the guardrail fix**

```bash
git add ai-platform-server/src/test/java/com/aiplatform/backend/service/ToolDefinitionServiceTest.java ai-platform-server/src/main/java/com/aiplatform/backend/service/ToolDefinitionService.java ai-platform-server/src/main/java/com/aiplatform/backend/entity/ProjectMcpIntegration.java
git commit -m "fix: align persistence enums with current schema"
```

### Task 2: Add the Canonical MySQL Migration Draft

**Files:**
- Create: `docs/db/migrations/2026-03-31-first-batch-schema.sql`
- Modify: `docs/db/ai_platform.sql`

- [ ] **Step 1: Create the migration draft for the first batch of schema changes**

```sql
ALTER TABLE knowledge_bases
    ADD COLUMN collection_id VARCHAR(128) NULL COMMENT '向量检索集合标识',
    ADD COLUMN vector_store VARCHAR(32) NULL COMMENT '向量库类型',
    ADD COLUMN embedding_dimension INT UNSIGNED NULL COMMENT '向量维度',
    ADD COLUMN inheritance_mode VARCHAR(32) NULL COMMENT '继承模式',
    ADD COLUMN visibility_scope VARCHAR(32) NULL COMMENT '可见范围',
    ADD COLUMN published_flag TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否已发布';

ALTER TABLE knowledge_bases
    ADD CONSTRAINT uk_knowledge_collection UNIQUE (collection_id);

CREATE TABLE skill_versions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    skill_id BIGINT UNSIGNED NOT NULL,
    version_no VARCHAR(32) NOT NULL,
    system_prompt TEXT NULL,
    execution_type VARCHAR(32) NULL,
    main_tool_id BIGINT UNSIGNED NULL,
    config_json JSON NULL,
    status VARCHAR(32) NOT NULL,
    submitted_by BIGINT UNSIGNED NULL,
    submitted_at DATETIME NULL,
    approved_by BIGINT UNSIGNED NULL,
    approved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_skill_version (skill_id, version_no)
);
```

- [ ] **Step 2: Add the remaining draft objects for tools, integrations, and project authorization**

```sql
CREATE TABLE tool_versions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tool_id BIGINT UNSIGNED NOT NULL,
    version_no VARCHAR(32) NOT NULL,
    input_schema JSON NOT NULL,
    output_schema JSON NULL,
    impl_type VARCHAR(32) NOT NULL,
    impl_config JSON NULL,
    status VARCHAR(32) NOT NULL,
    submitted_by BIGINT UNSIGNED NULL,
    submitted_at DATETIME NULL,
    approved_by BIGINT UNSIGNED NULL,
    approved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tool_version (tool_id, version_no)
);

CREATE TABLE integration_market_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    item_code VARCHAR(64) NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    item_type VARCHAR(32) NOT NULL,
    source_server_id BIGINT UNSIGNED NULL,
    category VARCHAR(64) NULL,
    icon_url VARCHAR(255) NULL,
    description VARCHAR(500) NULL,
    auth_type VARCHAR(32) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_item_code (item_code)
);
```

- [ ] **Step 3: Fold the same structural changes into `docs/db/ai_platform.sql`**

```sql
-- Keep seed SQL aligned with the new canonical structure.
-- Add the same columns/tables introduced in docs/db/migrations/2026-03-31-first-batch-schema.sql.
-- Do not remove old fields in this step; keep the file backward-readable.
```

- [ ] **Step 4: Review the migration draft for naming and enum consistency**

Run: `Select-String -Path 'docs/db/migrations/2026-03-31-first-batch-schema.sql' -Pattern 'INACTIVE'`

Expected: no matches, and the file should not contain placeholder sections.

- [ ] **Step 5: Commit the MySQL draft**

```bash
git add docs/db/migrations/2026-03-31-first-batch-schema.sql docs/db/ai_platform.sql
git commit -m "feat: draft first batch mysql schema evolution"
```

### Task 3: Sync the H2 Test Schema with the First Batch

**Files:**
- Modify: `ai-platform-server/src/test/resources/db/schema-h2.sql`
- Create: `ai-platform-server/src/test/java/com/aiplatform/backend/persistence/ProjectMcpIntegrationPersistenceTest.java`

- [ ] **Step 1: Write the failing persistence test for `ProjectMcpIntegration`**

```java
package com.aiplatform.backend.persistence;

import com.aiplatform.backend.entity.ProjectMcpIntegration;
import com.aiplatform.backend.mapper.ProjectMcpIntegrationMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MybatisTest
class ProjectMcpIntegrationPersistenceTest {

    @Autowired
    private ProjectMcpIntegrationMapper mapper;

    @Test
    void shouldPersistCustomConfigPermissionScopeAndConnectedAt() {
        ProjectMcpIntegration row = ProjectMcpIntegration.builder()
                .projectId(1L)
                .mcpServerId(2L)
                .customConfig("{\"board\":\"OMNI\"}")
                .permissionScope("readonly")
                .status("ACTIVE")
                .build();

        mapper.insert(row);
        ProjectMcpIntegration saved = mapper.selectById(row.getId());

        assertEquals("{\"board\":\"OMNI\"}", saved.getCustomConfig());
        assertEquals("readonly", saved.getPermissionScope());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails against the current H2 schema**

Run: `mvn -pl ai-platform-server -Dtest=ProjectMcpIntegrationPersistenceTest test`

Expected: FAIL because `schema-h2.sql` does not yet expose the full `project_mcp_integrations` column set.

- [ ] **Step 3: Update `schema-h2.sql` to include the evolved columns and new tables**

```sql
CREATE TABLE IF NOT EXISTS project_mcp_integrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    mcp_server_id BIGINT NOT NULL,
    custom_config CLOB,
    permission_scope VARCHAR(128),
    status VARCHAR(32),
    connected_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE knowledge_bases ADD COLUMN IF NOT EXISTS collection_id VARCHAR(128);
ALTER TABLE skills ADD COLUMN IF NOT EXISTS current_version_id BIGINT;
ALTER TABLE tool_definitions ADD COLUMN IF NOT EXISTS approval_status VARCHAR(32);
```

- [ ] **Step 4: Re-run the persistence test**

Run: `mvn -pl ai-platform-server -Dtest=ProjectMcpIntegrationPersistenceTest test`

Expected: PASS with no SQL grammar exceptions.

- [ ] **Step 5: Commit the H2 alignment**

```bash
git add ai-platform-server/src/test/resources/db/schema-h2.sql ai-platform-server/src/test/java/com/aiplatform/backend/persistence/ProjectMcpIntegrationPersistenceTest.java
git commit -m "test: align h2 schema with first batch persistence fields"
```

### Task 4: Sync Core Entities with the First-Batch Columns

**Files:**
- Modify: `ai-platform-server/src/main/java/com/aiplatform/backend/entity/KnowledgeBase.java`
- Modify: `ai-platform-server/src/main/java/com/aiplatform/backend/entity/Skill.java`
- Modify: `ai-platform-server/src/main/java/com/aiplatform/backend/entity/ToolDefinition.java`

- [ ] **Step 1: Add the first-batch fields to `KnowledgeBase`**

```java
private String collectionId;
private String vectorStore;
private Integer embeddingDimension;
private String inheritanceMode;
private String visibilityScope;
private Boolean publishedFlag;
```

- [ ] **Step 2: Add approval/version ownership fields to `Skill`**

```java
private Long currentVersionId;
private String approvalStatus;
private String sourceType;
private String ownerType;
private Long ownerId;
```

- [ ] **Step 3: Add approval/version/integration fields to `ToolDefinition` and fix the status comment**

```java
private String toolType;
private String providerType;
private Long integrationId;
private Long currentVersionId;
private String approvalStatus;
private String visibilityScope;

/** 状态：ACTIVE / DISABLED */
private String status;
```

- [ ] **Step 4: Run compilation to catch field or mapper mismatches**

Run: `mvn -pl ai-platform-server -DskipTests compile`

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit the entity synchronization**

```bash
git add ai-platform-server/src/main/java/com/aiplatform/backend/entity/KnowledgeBase.java ai-platform-server/src/main/java/com/aiplatform/backend/entity/Skill.java ai-platform-server/src/main/java/com/aiplatform/backend/entity/ToolDefinition.java
git commit -m "refactor: sync entities with first batch schema fields"
```

### Task 5: Add the New Relation and Authorization Tables to the Draft

**Files:**
- Modify: `docs/db/migrations/2026-03-31-first-batch-schema.sql`
- Modify: `docs/db/ai_platform.sql`

- [ ] **Step 1: Add skill relation tables to the draft**

```sql
CREATE TABLE skill_version_tools (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    skill_version_id BIGINT UNSIGNED NOT NULL,
    tool_id BIGINT UNSIGNED NOT NULL,
    relation_type VARCHAR(32) NOT NULL,
    sort_order INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_skill_version_tool (skill_version_id, tool_id, relation_type)
);

CREATE TABLE skill_version_knowledge_refs (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    skill_version_id BIGINT UNSIGNED NOT NULL,
    knowledge_base_id BIGINT UNSIGNED NULL,
    collection_id VARCHAR(128) NULL,
    top_k INT UNSIGNED NULL,
    prompt_template TEXT NULL,
    sort_order INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
```

- [ ] **Step 2: Add project authorization tables to the draft**

```sql
CREATE TABLE project_role_templates (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    project_id BIGINT UNSIGNED NOT NULL,
    role_key VARCHAR(64) NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    description VARCHAR(255) NULL,
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_role_key (project_id, role_key)
);

CREATE TABLE project_member_resource_grants (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    project_member_id BIGINT UNSIGNED NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_id BIGINT UNSIGNED NOT NULL,
    grant_level VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_resource (project_member_id, resource_type, resource_id)
);
```

- [ ] **Step 3: Add the remaining integration authorization table**

```sql
CREATE TABLE mcp_server_authorizations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    mcp_server_id BIGINT UNSIGNED NOT NULL,
    subject_type VARCHAR(32) NOT NULL,
    subject_id BIGINT UNSIGNED NOT NULL,
    auth_type VARCHAR(32) NOT NULL,
    auth_payload JSON NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    expires_at DATETIME NULL,
    last_verified_at DATETIME NULL,
    created_by BIGINT UNSIGNED NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
```

- [ ] **Step 4: Check the final draft for duplicate names and missing indexes**

Run: `Select-String -Path 'docs/db/migrations/2026-03-31-first-batch-schema.sql' -Pattern 'CREATE TABLE|UNIQUE KEY|INDEX'`

Expected: each new table includes at least its primary key and required unique/index definitions.

- [ ] **Step 5: Commit the relation-table draft**

```bash
git add docs/db/migrations/2026-03-31-first-batch-schema.sql docs/db/ai_platform.sql
git commit -m "feat: draft relation and authorization tables"
```

### Task 6: Final Verification and Documentation Cross-Check

**Files:**
- Modify: `docs/superpowers/specs/2026-03-31-database-schema-optimization-design.md` (only if mismatches are found)
- Modify: `docs/superpowers/plans/2026-03-31-first-batch-database-schema-evolution.md` (check off completed items during execution)

- [ ] **Step 1: Run the focused regression tests**

Run: `mvn -pl ai-platform-server -Dtest=ToolDefinitionServiceTest,ProjectMcpIntegrationPersistenceTest test`

Expected: PASS.

- [ ] **Step 2: Run module compilation**

Run: `mvn -pl ai-platform-server -DskipTests compile`

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Verify the design doc still matches the implemented first batch**

Run: `git diff -- docs/superpowers/specs/2026-03-31-database-schema-optimization-design.md docs/db/migrations/2026-03-31-first-batch-schema.sql`

Expected: any differences should be deliberate terminology differences, not missing tables or fields.

- [ ] **Step 4: Update the spec only if implementation forced a naming correction**

```markdown
- If `tool_definitions` must stay named as-is for compatibility, keep the spec language explicit that this batch preserves the table name.
- If H2 requires a type downgrade from `JSON` to `CLOB`, note that this is test-schema-only and not a MySQL contract change.
```

- [ ] **Step 5: Commit the verified first batch**

```bash
git add docs/superpowers/specs/2026-03-31-database-schema-optimization-design.md docs/superpowers/plans/2026-03-31-first-batch-database-schema-evolution.md
git commit -m "docs: verify first batch schema evolution plan"
```
