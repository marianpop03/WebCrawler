package com.example.webcrawler.repository;

import com.example.webcrawler.entity.PageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface PageContentRepository extends JpaRepository<PageContent, Long> {
    @Query("SELECT p FROM PageContent p JOIN p.url u WHERE " +
            "(LOWER(p.contentText) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.pageTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:domain IS NULL OR :domain = '' OR u.url LIKE %:domain%)")
    List<PageContent> searchByKeywordAndDomain(@Param("keyword") String keyword, @Param("domain") String domain);
    List<PageContent> findByContentTextContainingIgnoreCaseOrPageTitleContainingIgnoreCase(String contentKeyword, String titleKeyword);
}