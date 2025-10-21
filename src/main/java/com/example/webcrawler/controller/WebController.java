package com.example.webcrawler.controller;

import com.example.webcrawler.entity.CrawlerConfig;
import com.example.webcrawler.entity.PageContent;
import com.example.webcrawler.entity.Url;
import com.example.webcrawler.repository.CrawlerConfigRepository;
import com.example.webcrawler.repository.UrlRepository;
import com.example.webcrawler.service.CrawlerService;
import com.example.webcrawler.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class WebController {

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private CrawlerConfigRepository crawlerConfigRepository;

    @Autowired
    private UrlRepository urlRepository;

    /**
     * Afișează pagina principală cu formularul de configurare a crawler-ului
     * și o listă de configurații existente.
     * @param model Modelul Thymeleaf.
     * @return Numele template-ului HTML (index.html).
     */
    @GetMapping("/")
    public String index(Model model) {

        if (!model.containsAttribute("crawlerConfig")) {
            model.addAttribute("crawlerConfig", new CrawlerConfig());
        }
        model.addAttribute("configs", crawlerConfigRepository.findAll());
        model.addAttribute("isCrawlerRunning", crawlerService.isRunning());
        return "index";
    }

    /**
     * Salvează o nouă configurație de crawler sau actualizează una existentă.
     * @param crawlerConfig Obiectul CrawlerConfig trimis din formular.
     * @param redirectAttributes Atribute pentru redirecționare (mesaje flash).
     * @return Redirecționează către pagina principală.
     */
    @PostMapping("/saveConfig")
    public String saveConfig(@ModelAttribute CrawlerConfig crawlerConfig, RedirectAttributes redirectAttributes) {

        if (crawlerConfig.getStatus() == null) {
            crawlerConfig.setStatus(CrawlerConfig.ConfigStatus.ACTIVE);
        }
        crawlerConfigRepository.save(crawlerConfig);
        redirectAttributes.addFlashAttribute("message", "Configurația a fost salvată cu succes!");
        return "redirect:/";
    }

    /**
     * Încarcă o configurație existentă în formular pentru editare.
     * @param id ID-ul configurației.
     * @param model Modelul Thymeleaf.
     * @return Redirecționează către pagina principală cu configurația preîncărcată.
     */
    @GetMapping("/editConfig")
    public String editConfig(@RequestParam Long id, Model model) {
        Optional<CrawlerConfig> configOptional = crawlerConfigRepository.findById(id);
        configOptional.ifPresent(config -> model.addAttribute("crawlerConfig", config));
        return index(model);
    }

    /**
     * Șterge o configurație de crawler.
     * @param id ID-ul configurației de șters.
     * @param redirectAttributes Atribute pentru redirecționare.
     * @return Redirecționează către pagina principală.
     */
    @GetMapping("/deleteConfig")
    public String deleteConfig(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        crawlerConfigRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Configurația a fost ștearsă.");
        return "redirect:/";
    }

    /**
     * Inițiază procesul de crawling.
     * @param configId ID-ul configurației de utilizat.
     * @param redirectAttributes Atribute pentru redirecționare.
     * @return Redirecționează către pagina principală sau către pagina de status.
     */
    @PostMapping("/startCrawler")
    public String startCrawler(@RequestParam Long configId, RedirectAttributes redirectAttributes) {
        if (crawlerService.isRunning()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Crawler-ul este deja pornit.");
        } else {
            crawlerService.startCrawling(configId);
            redirectAttributes.addFlashAttribute("message", "Crawler-ul a pornit!");
        }
        return "redirect:/";
    }

    /**
     * Oprește procesul de crawling.
     * @param redirectAttributes Atribute pentru redirecționare.
     * @return Redirecționează către pagina principală.
     */
    @PostMapping("/stopCrawler")
    public String stopCrawler(RedirectAttributes redirectAttributes) {
        if (!crawlerService.isRunning()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Crawler-ul nu este pornit.");
        } else {
            crawlerService.stopCrawling();
            redirectAttributes.addFlashAttribute("message", "Crawler-ul a fost oprit.");
        }
        return "redirect:/";
    }

    /**
     * Afișează pagina cu statusul crawler-ului (URL-uri vizitate, în așteptare).
     * @param model Modelul Thymeleaf.
     * @return Numele template-ului HTML (status.html).
     */
    @GetMapping("/status")
    public String status(Model model) {
        List<Url> visitedUrls = urlRepository.findByStatus(Url.UrlStatus.VISITED);
        List<Url> pendingUrls = urlRepository.findByStatus(Url.UrlStatus.PENDING);
        List<Url> failedUrls = urlRepository.findByStatus(Url.UrlStatus.FAILED);

        model.addAttribute("visitedUrls", visitedUrls);
        model.addAttribute("pendingUrls", pendingUrls);
        model.addAttribute("failedUrls", failedUrls);
        model.addAttribute("isCrawlerRunning", crawlerService.isRunning());

        return "status";
    }

    /**
     * Afișează pagina de căutare și rezultatele căutării.
     * @param keyword Cuvântul cheie de căutat (opțional).
     * @param model Modelul Thymeleaf.
     * @return Numele template-ului HTML (searchResults.html).
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        List<PageContent> searchResults = Collections.emptyList();
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchResults = searchService.searchByKeyword(keyword);
        }
        model.addAttribute("keyword", keyword); // Pentru a pre-completa câmpul de căutare
        model.addAttribute("searchResults", searchResults);
        return "searchResults";
    }
}