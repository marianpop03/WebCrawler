package com.example.webcrawler.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class CrawlerServiceUnitTest {

    @InjectMocks
    private CrawlerService crawlerService;

    @Test
    void testShouldExclude_ImagesAndPdfs() {
        // GIVEN: Configurăm serviciul să excludă imagini și PDF-uri


        // WHEN & THEN
        assertTrue(crawlerService.isExcluded("https://example.com/image.jpg"), "Ar trebui să excludă .jpg");
        assertTrue(crawlerService.isExcluded("https://example.com/file.pdf"), "Ar trebui să excludă .pdf");
        assertFalse(crawlerService.isExcluded("https://example.com/page.html"), "Nu ar trebui să excludă pagini HTML");
    }
    @Test
    void testGetDomain_Extraction() {
        String url = "https://subdomeniu.exemplu.ro/pagina?id=1";
        String domain = crawlerService.getDomain(url);

        assertEquals("exemplu.ro", domain, "Domeniul extras ar trebui să fie exemplul.ro");
    }

}