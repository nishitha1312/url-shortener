package com.urlshortener.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "click_events", indexes = {
    @Index(name = "idx_click_short_code", columnList = "shortCode"),
    @Index(name = "idx_click_timestamp", columnList = "clickedAt")
})
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String shortCode;

    @Column
    @CreationTimestamp
    private LocalDateTime clickedAt;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    @Column(length = 64)
    private String country;

    @Column(length = 64)
    private String referer;

    public ClickEvent() {}

    // Getters
    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public LocalDateTime getClickedAt() { return clickedAt; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getCountry() { return country; }
    public String getReferer() { return referer; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    public void setClickedAt(LocalDateTime clickedAt) { this.clickedAt = clickedAt; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setCountry(String country) { this.country = country; }
    public void setReferer(String referer) { this.referer = referer; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ClickEvent e = new ClickEvent();
        public Builder shortCode(String v) { e.shortCode = v; return this; }
        public Builder ipAddress(String v) { e.ipAddress = v; return this; }
        public Builder userAgent(String v) { e.userAgent = v; return this; }
        public Builder country(String v) { e.country = v; return this; }
        public Builder referer(String v) { e.referer = v; return this; }
        public ClickEvent build() { return e; }
    }
}
