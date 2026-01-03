package com.example.webcrawler.repository;

import com.example.webcrawler.entity.CrawlerConfig;
import com.example.webcrawler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlerConfigRepository extends JpaRepository<CrawlerConfig, Long> {

    List<CrawlerConfig> findByUser(User user);
}