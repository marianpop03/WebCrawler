package com.example.webcrawler.repository;

import com.example.webcrawler.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    // Metode personalizate (custom queries) dacă avem nevoie
    // Spring Data JPA poate genera automat implementarea pentru aceste metode
    // pe baza numelui metodei.

    // Găsește un URL după string-ul său
    Optional<Url> findByUrl(String url);

    // Găsește toate URL-urile cu un anumit status
    List<Url> findByStatus(Url.UrlStatus status);

    // Găsește toate URL-urile care au deepScan activat și un anumit status
    List<Url> findByDeepScanTrueAndStatus(Url.UrlStatus status);
}