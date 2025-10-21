package com.example.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "page_contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Url url;

    @Column(columnDefinition = "LONGTEXT")
    private String contentText;

    @Column(length = 512)
    private String pageTitle;


    @Column(columnDefinition = "TEXT")
    private String metadata;

    public PageContent(Url url, String contentText, String pageTitle, String metadata) {
        this.url = url;
        this.contentText = contentText;
        this.pageTitle = pageTitle;
        this.metadata = metadata;
    }
}