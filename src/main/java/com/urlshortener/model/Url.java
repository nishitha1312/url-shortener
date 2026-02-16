package com.urlshortener.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "urls", indexes = {
    @Index(name = "idx_short_code", columnList = "shortCode", unique = true),
    @Index(name = "idx_expires_at", columnList = "expiresAt"),
    @Index(name = "idx_created_by", columnList = "createdBy")
})
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 64)
    private String createdBy;

    @Column(nullable = false)
    private long clickCount = 0;

    @Column(length = 255)
    private String customAlias;

    @Column(length = 255)
    private String title;

    public Url() {}

    // Getters
    public Long getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public String getCreatedBy() { return createdBy; }
    public long getClickCount() { return clickCount; }
    public String getCustomAlias() { return customAlias; }
    public String getTitle() { return title; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setClickCount(long clickCount) { this.clickCount = clickCount; }
    public void setCustomAlias(String customAlias) { this.customAlias = customAlias; }
    public void setTitle(String title) { this.title = title; }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementClickCount() { this.clickCount++; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Url url = new Url();
        public Builder originalUrl(String v) { url.originalUrl = v; return this; }
        public Builder shortCode(String v) { url.shortCode = v; return this; }
        public Builder expiresAt(LocalDateTime v) { url.expiresAt = v; return this; }
        public Builder active(boolean v) { url.active = v; return this; }
        public Builder createdBy(String v) { url.createdBy = v; return this; }
        public Builder customAlias(String v) { url.customAlias = v; return this; }
        public Builder title(String v) { url.title = v; return this; }
        public Url build() { return url; }
    }
}
