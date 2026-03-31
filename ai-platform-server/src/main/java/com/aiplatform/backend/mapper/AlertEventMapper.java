package com.aiplatform.backend.mapper;

import com.aiplatform.backend.entity.AlertEvent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface AlertEventMapper extends BaseMapper<AlertEvent> {

    /**
     * 本月「需关注」告警事件数：按关联规则的 severity 统计（CRITICAL / HIGH / MEDIUM，不含 LOW）。
     */
    @Select("""
            SELECT COUNT(1) FROM alert_events e
            INNER JOIN alert_rules r ON e.rule_id = r.id
            WHERE e.project_id = #{projectId}
            AND r.severity IN ('CRITICAL', 'HIGH', 'MEDIUM')
            AND e.created_at >= #{since}
            """)
    long countElevatedSeverityEventsSince(@Param("projectId") Long projectId, @Param("since") LocalDateTime since);

    /**
     * 分页列表：可选按状态、项目过滤；severity 过滤在关联规则上生效。
     */
    @Select("""
            <script>
            SELECT e.id, e.rule_id, e.project_id, e.user_id, e.trigger_value, e.message, e.notified_channels,
            r.severity AS severity, e.status, e.created_at, e.resolved_at
            FROM alert_events e
            LEFT JOIN alert_rules r ON e.rule_id = r.id
            <where>
            <if test="status != null and status != ''">AND e.status = #{status}</if>
            <if test="severity != null and severity != ''">AND r.severity = #{severity}</if>
            <if test="projectId != null">AND e.project_id = #{projectId}</if>
            </where>
            ORDER BY e.id DESC
            </script>
            """)
    IPage<AlertEvent> selectPageWithRule(Page<AlertEvent> page,
                                         @Param("status") String status,
                                         @Param("severity") String severity,
                                         @Param("projectId") Long projectId);
}
