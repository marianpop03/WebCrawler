package com.example.webcrawler.repository;

import com.example.webcrawler.entity.PageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface PageContentRepository extends JpaRepository<PageContent, Long> {
    List<PageContent> findByContentTextContainingIgnoreCaseOrPageTitleContainingIgnoreCase(String contentKeyword, String titleKeyword);
}