package com.example.webcrawler.repository;

import com.example.webcrawler.entity.CrawlerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlerConfigRepository extends JpaRepository<CrawlerConfig, Long> {

}