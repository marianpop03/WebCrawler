package com.example.webcrawler.service;

import com.example.webcrawler.entity.PageContent;
import com.example.webcrawler.repository.PageContentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private PageContentRepository pageContentRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    void testSearchByKeyword_Success() {
        // GIVEN
        String keyword = "java";
        String domain = "example.com";
        List<PageContent> mockResults = List.of(new PageContent());
        when(pageContentRepository.searchByKeywordAndDomain(keyword, domain)).thenReturn(mockResults);

        // WHEN
        List<PageContent> results = searchService.searchByKeyword(keyword, domain);

        // THEN
        assertEquals(1, results.size());
        verify(pageContentRepository, times(1)).searchByKeywordAndDomain(keyword, domain);
    }

    @Test
    void testSearchByKeyword_EmptyKeyword() {
        // WHEN
        List<PageContent> results = searchService.searchByKeyword("", null);

        // THEN
        assertTrue(results.isEmpty());
        verifyNoInteractions(pageContentRepository);
    }
}