package org.chud.springuniapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity{

    // SHA-256 hex of the value handed to the client. The raw value exists
    // only in the HTTP response that created it and in the client's storage.
    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    //null = still alive
    @Column(name = "revoked_at")
    private Instant revokedAt;

    // Hash of the replacement key. Lets you walk the chain and kill all previous ones
    @Column(name = "replaced_by_hash", length = 64)
    private String replacedByHash;

    public RefreshToken(String keyHash, User user, Instant expiresAt) {
        this.keyHash = keyHash;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public boolean isLive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void revoke(Instant now) {
        if (revokedAt == null) {
            this.revokedAt = now;
        }
    }

    public void rotateInto(String successorHash, Instant now) {
        revoke(now);
        this.replacedByHash = successorHash;
    }
}
