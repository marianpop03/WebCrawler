package com.example.webcrawler.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "urls") // Numele tabelului în baza de date
@Data // Generates getters, setters, toString, equals, and hashCode methods
@NoArgsConstructor // Generates a constructor with no arguments
@AllArgsConstructor // Generates a constructor with all arguments
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2048) // Limită de 2048 pentru URL-uri lungi
    private String url;

    private LocalDateTime visitDate;

    private boolean deepScan; // True dacă URL-ul a fost vizitat în cadrul unui deep scan

    @Enumerated(EnumType.STRING) // Stochează enum-ul ca string în baza de date
    private UrlStatus status; // PENDING, VISITED, FAILED

    @OneToMany(mappedBy = "url", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PageContent> pageContents; // Asociază conținutul paginii cu URL-ul

    // Enum pentru statusul URL-ului
    public enum UrlStatus {
        PENDING,
        VISITED,
        FAILED
    }
}