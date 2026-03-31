package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.PlatformCredential;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 根据用户平台凭证推导项目成员列表中的「凭证状态」展示字段。
 */
public final class ProjectMemberCredentialView {

    private ProjectMemberCredentialView() {
    }

    /**
     * 剩余自然日 ≤ 此天数且仍有效时，{@link #status} 为 {@code EXPIRING_SOON}（如「3 天后过期」）。
     */
    public static final int EXPIRING_SOON_WITHIN_DAYS = 7;

    /**
     * 凭证状态码，供前端映射文案/颜色：
     * {@code NONE} 无凭证；{@code VALID} 有效；{@code EXPIRING_SOON} 即将过期；
     * {@code EXPIRED} 已过期；{@code REVOKED} 已吊销；{@code DISABLED} 已停用。
     */
    public static String status(PlatformCredential c) {
        if (c == null) {
            return "NONE";
        }
        String s = c.getStatus();
        if ("REVOKED".equals(s)) {
            return "REVOKED";
        }
        if ("DISABLED".equals(s)) {
            return "DISABLED";
        }
        if ("EXPIRED".equals(s)) {
            return "EXPIRED";
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime exp = c.getExpiresAt();
        if (exp != null && !exp.isAfter(now)) {
            return "EXPIRED";
        }
        if (!"ACTIVE".equals(s)) {
            return "DISABLED";
        }
        if (exp != null) {
            long days = ChronoUnit.DAYS.between(now.toLocalDate(), exp.toLocalDate());
            if (days >= 0 && days <= EXPIRING_SOON_WITHIN_DAYS) {
                return "EXPIRING_SOON";
            }
        }
        return "VALID";
    }

    /** 距离过期剩余整自然日数；已过期或无过期时间则为 {@code null}。 */
    public static Integer expiresInDays(PlatformCredential c) {
        if (c == null || c.getExpiresAt() == null) {
            return null;
        }
        LocalDateTime exp = c.getExpiresAt();
        LocalDateTime now = LocalDateTime.now();
        if (!exp.isAfter(now)) {
            return null;
        }
        int days = (int) ChronoUnit.DAYS.between(now.toLocalDate(), exp.toLocalDate());
        return Math.max(days, 0);
    }

    public static LocalDateTime expiresAt(PlatformCredential c) {
        return c != null ? c.getExpiresAt() : null;
    }
}
