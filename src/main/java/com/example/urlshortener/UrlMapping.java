package com.example.urlshortener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_mappings")
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    private int clickCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UrlMapping() {
    }

    public UrlMapping(String code, String originalUrl) {
        this.code = code;
        this.originalUrl = originalUrl;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public int getClickCount() {
        return clickCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void incrementClickCount() {
        this.clickCount++;
    }
}
