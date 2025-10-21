package com.example.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    private LocalDateTime visitDate;

    private boolean deepScan;

    @Enumerated(EnumType.STRING)
    private UrlStatus status;

    @OneToMany(mappedBy = "url", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PageContent> pageContents;

    public enum UrlStatus {
        PENDING,
        VISITED,
        FAILED
    }
}