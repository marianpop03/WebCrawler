package com.example.webcrawler.service;

import com.example.webcrawler.entity.PageContent;
import com.example.webcrawler.repository.PageContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SearchService {

    @Autowired
    private PageContentRepository pageContentRepository;

    /**
     * Caută pagini care conțin un anumit cuvânt cheie în titlu sau în conținutul text.
     * Căutarea este case-insensitive.
     *
     * @param keyword Cuvântul cheie de căutat.
     * @param domain
     * @return O listă de PageContent care se potrivesc criteriilor.
     */
    public List<PageContent> searchByKeyword(String keyword, String domain) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        String trimmedKeyword = keyword.trim();
        String trimmedDomain = (domain != null) ? domain.trim() : null;

        log.info("Caut keyword: '{}' pe domeniul: '{}'", trimmedKeyword, trimmedDomain);

        // Apelăm noua metodă din repository
        return pageContentRepository.searchByKeywordAndDomain(trimmedKeyword, trimmedDomain);
    }
}