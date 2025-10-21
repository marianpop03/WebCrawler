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
     * @return O listă de PageContent care se potrivesc criteriilor.
     */
    public List<PageContent> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("Cuvântul cheie pentru căutare este gol sau null.");
            return List.of();
        }

        String trimmedKeyword = keyword.trim();
        log.info("Caut pagini pentru cuvântul cheie: '{}'", trimmedKeyword);
        List<PageContent> results = pageContentRepository.findByContentTextContainingIgnoreCaseOrPageTitleContainingIgnoreCase(trimmedKeyword, trimmedKeyword);

        log.info("S-au găsit {} rezultate pentru cuvântul cheie: '{}'", results.size(), trimmedKeyword);
        return results;
    }
}