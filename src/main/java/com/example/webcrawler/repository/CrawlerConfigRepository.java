package com.example.webcrawler.repository;

import com.example.webcrawler.entity.CrawlerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrawlerConfigRepository extends JpaRepository<CrawlerConfig, Long> {

    Optional<CrawlerConfig> findByStatus(CrawlerConfig.ConfigStatus status);
}