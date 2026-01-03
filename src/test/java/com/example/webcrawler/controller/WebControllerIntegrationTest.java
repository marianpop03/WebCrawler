package com.example.webcrawler.controller;

import com.example.webcrawler.service.CrawlerService;
import com.example.webcrawler.service.SearchService;
import com.example.webcrawler.repository.CrawlerConfigRepository;
import com.example.webcrawler.repository.UrlRepository;
import com.example.webcrawler.repository.UserRepository;
import com.example.webcrawler.repository.PageContentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
class WebControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CrawlerService crawlerService;
    @MockBean private SearchService searchService;
    @MockBean private CrawlerConfigRepository crawlerConfigRepository;
    @MockBean private UrlRepository urlRepository;
    @MockBean private PageContentRepository pageContentRepository;
    @MockBean private UserRepository userRepository;



    @Test
    void testIndexPage_UnauthorizedRedirect() throws Exception {
        mockMvc.perform(get("/").accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testIndexPage_AuthenticatedSuccess() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("username"));
    }

    @Test
    @WithMockUser(username = "marian")
    void testSearchRoute_Success() throws Exception {
        mockMvc.perform(get("/search").param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(view().name("searchResults"))
                .andExpect(model().attributeExists("searchResults"));
    }
}