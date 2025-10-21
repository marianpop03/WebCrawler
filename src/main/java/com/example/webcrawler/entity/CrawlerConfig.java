package com.example.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawler_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4096)
    private String seedUrls;

    private Integer maxDepth;

    @Column(length = 1024)
    private String keywordsToSearch;

    private Integer threadCount;

    private boolean excludeImages;
    private boolean excludePdfs;

    private LocalDateTime createdAt;
    private LocalDateTime lastRunAt;


    @Enumerated(EnumType.STRING)
    private ConfigStatus status;

    public enum ConfigStatus {
        ACTIVE,
        INACTIVE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}