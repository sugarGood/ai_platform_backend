package com.aiplatform.backend.dto;

import com.aiplatform.backend.entity.PlatformCredential;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMemberCredentialViewTest {

    @Test
    void statusNoneWhenNoCredential() {
        assertThat(ProjectMemberCredentialView.status(null)).isEqualTo("NONE");
    }

    @Test
    void statusValidWhenActiveAndFarExpiry() {
        PlatformCredential c = new PlatformCredential();
        c.setStatus("ACTIVE");
        c.setExpiresAt(LocalDateTime.now().plusDays(30));
        assertThat(ProjectMemberCredentialView.status(c)).isEqualTo("VALID");
        assertThat(ProjectMemberCredentialView.expiresInDays(c)).isEqualTo(30);
    }

    @Test
    void statusExpiringSoonWithinWindow() {
        PlatformCredential c = new PlatformCredential();
        c.setStatus("ACTIVE");
        c.setExpiresAt(LocalDateTime.now().plusDays(3));
        assertThat(ProjectMemberCredentialView.status(c)).isEqualTo("EXPIRING_SOON");
        assertThat(ProjectMemberCredentialView.expiresInDays(c)).isEqualTo(3);
    }

    @Test
    void statusExpiredWhenPastExpiry() {
        PlatformCredential c = new PlatformCredential();
        c.setStatus("ACTIVE");
        c.setExpiresAt(LocalDateTime.now().minusDays(1));
        assertThat(ProjectMemberCredentialView.status(c)).isEqualTo("EXPIRED");
        assertThat(ProjectMemberCredentialView.expiresInDays(c)).isNull();
    }
}
