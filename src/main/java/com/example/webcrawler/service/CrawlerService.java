package com.example.webcrawler.service;

import com.example.webcrawler.entity.CrawlerConfig;
import com.example.webcrawler.entity.PageContent;
import com.example.webcrawler.entity.Url;
import com.example.webcrawler.repository.CrawlerConfigRepository;
import com.example.webcrawler.repository.PageContentRepository;
import com.example.webcrawler.repository.UrlRepository;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class CrawlerService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PageContentRepository pageContentRepository;

    @Autowired
    private CrawlerConfigRepository crawlerConfigRepository;

    private final Queue<String> urlQueue = new ConcurrentLinkedQueue<>();
    private final Set<String> visitedUrls = Collections.synchronizedSet(new HashSet<>());
    private ExecutorService executorService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private CrawlerConfig currentConfig;

    /**
     * Inițiază procesul de crawling.
     * @param configId ID-ul configurației de crawler de utilizat.
     */
    public void startCrawling(Long configId) {
        if (isRunning.get()) {
            log.warn("Crawler-ul este deja pornit.");
            return;
        }

        Optional<CrawlerConfig> configOptional = crawlerConfigRepository.findById(configId);
        if (configOptional.isEmpty()) {
            log.error("Configurația cu ID-ul {} nu a fost găsită.", configId);
            return;
        }
        currentConfig = configOptional.get();

        int threadCount = currentConfig.getThreadCount() != null && currentConfig.getThreadCount() > 0 ?
                currentConfig.getThreadCount() : 5;
        executorService = Executors.newFixedThreadPool(threadCount);

        if (currentConfig.getSeedUrls() != null && !currentConfig.getSeedUrls().isEmpty()) {
            String[] seeds = currentConfig.getSeedUrls().split(",");
            for (String seed : seeds) {
                String trimmedSeed = seed.trim();
                if (!trimmedSeed.isEmpty() && isValidUrl(trimmedSeed)) {
                    urlQueue.offer(trimmedSeed);
                    log.info("Adăugat URL-ul inițial în coadă: {}", trimmedSeed);
                }
            }
        } else {
            log.warn("Niciun URL inițial (seed URL) configurat.");
            return;
        }

        isRunning.set(true);
        currentConfig.setLastRunAt(LocalDateTime.now());
        crawlerConfigRepository.save(currentConfig);

        log.info("Crawler-ul a pornit cu configurația ID: {}", currentConfig.getId());

        // Trimitem status inițial
        sendCrawlerStatusUpdate();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(this::crawlWorker);
        }
    }

    /**
     * Oprește procesul de crawling.
     */
    public void stopCrawling() {
        if (!isRunning.get()) {
            log.warn("Crawler-ul nu este pornit.");
            return;
        }

        isRunning.set(false);
        if (executorService != null) {
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("ExecutorService nu s-a oprit în 5 secunde.");
                }
            } catch (InterruptedException e) {
                log.error("Eroare la oprirea ExecutorService: {}", e.getMessage());
                Thread.currentThread().interrupt();
            } finally {
                executorService = null;
            }
        }
        log.info("Crawler-ul a fost oprit.");
        urlQueue.clear();
        visitedUrls.clear();
        currentConfig = null;
        sendCrawlerStatusUpdate();
    }

    /**
     * Verifică dacă crawler-ul rulează.
     * @return true dacă rulează, false altfel.
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * Worker-ul care preia URL-uri din coadă și le procesează.
     */
    private void crawlWorker() {
        while (isRunning.get()) {
            String urlToCrawl = urlQueue.poll();
            if (urlToCrawl == null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            if (visitedUrls.contains(urlToCrawl)) {
                log.debug("URL deja vizitat sau în procesare: {}", urlToCrawl);
                continue;
            }

            visitedUrls.add(urlToCrawl);

            try {
                processUrl(urlToCrawl, 0);
            } catch (Exception e) {
                log.error("Eroare la procesarea URL-ului {}: {}", urlToCrawl, e.getMessage());
                saveUrlStatus(urlToCrawl, Url.UrlStatus.FAILED);
            }
        }
        log.info("Worker thread oprit.");
    }

    /**
     * Procesează un singur URL: descarcă conținutul, extrage link-uri și salvează.
     * @param currentUrl URL-ul de procesat.
     * @param depth Adâncimea curentă de crawling.
     */
    @Transactional
    protected void processUrl(String currentUrl, int depth) throws IOException, URISyntaxException {
        if (!isRunning.get() || depth > currentConfig.getMaxDepth()) {
            log.debug("Limită de adâncime ({}) atinsă sau crawler oprit pentru URL: {}", currentConfig.getMaxDepth(), currentUrl);
            return;
        }

        Optional<Url> existingUrlOpt = urlRepository.findByUrl(currentUrl);
        if (existingUrlOpt.isPresent() && existingUrlOpt.get().getStatus() == Url.UrlStatus.VISITED) {
            log.debug("URL {} a fost deja vizitat și salvat în DB.", currentUrl);
            return;
        }

        log.info("Procesez URL-ul (adâncime {}): {}", depth, currentUrl);

        Document doc;
        try {
            doc = Jsoup.connect(currentUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                    .timeout(10 * 1000)
                    .get();
        } catch (IOException e) {
            log.error("Eroare la descărcarea URL-ului {}: {}", currentUrl, e.getMessage());
            saveUrlStatus(currentUrl, Url.UrlStatus.FAILED);
            return;
        }

        Url urlEntity = existingUrlOpt.orElseGet(() -> {
            Url newUrl = new Url();
            newUrl.setUrl(currentUrl);
            newUrl.setDeepScan(depth > 0);
            return newUrl;
        });
        urlEntity.setVisitDate(LocalDateTime.now());
        urlEntity.setStatus(Url.UrlStatus.VISITED);
        urlRepository.save(urlEntity);

        PageContent pageContent = new PageContent(
                urlEntity,
                doc.body().text(),
                doc.title(),
                extractMetadata(doc)
        );
        pageContentRepository.save(pageContent);

        log.info("Conținut salvat pentru URL: {}", currentUrl);


        sendCrawlerStatusUpdate(currentUrl, Url.UrlStatus.VISITED);

        if (currentConfig.getMaxDepth() != null && currentConfig.getMaxDepth() > depth) {
            extractLinks(doc, currentUrl, depth);
        }
    }

    /**
     * Extrage link-urile dintr-un document HTML și le adaugă în coadă.
     */
    private void extractLinks(Document doc, String baseUrl, int currentDepth) throws URISyntaxException {
        Elements links = doc.select("a[href]");
        String seedDomain = "";
        if (currentConfig.isStayOnDomain() && currentConfig.getSeedUrls() != null) {
            String firstSeed = currentConfig.getSeedUrls().split(",")[0].trim();
            seedDomain = getDomainName(firstSeed);
        }

        for (Element link : links) {
            String absUrl = link.attr("abs:href");
            if (!isValidUrl(absUrl) || isFileUrl(absUrl)) {
                continue;
            }

            if (currentConfig.isStayOnDomain() && !seedDomain.isEmpty()) {
                String linkDomain = getDomainName(absUrl);

                if (!linkDomain.contains(seedDomain)) {
                    log.debug("URL ignorat (domeniu extern): {}", absUrl);
                    continue;
                }
            }

            if (!visitedUrls.contains(absUrl) && !urlQueue.contains(absUrl)) {
                urlQueue.offer(absUrl);
                log.debug("Adăugat link nou în coadă (adâncime {}): {}", currentDepth + 1, absUrl);
            }
        }
    }

    private String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            if (domain != null) {
                return domain.startsWith("www.") ? domain.substring(4) : domain;
            }
        } catch (URISyntaxException e) {
            log.warn("Nu s-a putut extrage domeniul din: {}", url);
        }
        return "";
    }
    /**
     * Salvează sau actualizează statusul unui URL în baza de date.
     */
    private void saveUrlStatus(String urlString, Url.UrlStatus status) {
        Url urlEntity = urlRepository.findByUrl(urlString)
                .orElseGet(() -> {
                    Url newUrl = new Url();
                    newUrl.setUrl(urlString);
                    newUrl.setDeepScan(false);
                    return newUrl;
                });
        urlEntity.setStatus(status);
        urlEntity.setVisitDate(LocalDateTime.now());
        urlRepository.save(urlEntity);
        sendCrawlerStatusUpdate(urlString, status);
    }

    /**
     * Extrage metadata (description, keywords) din tag-urile HTML.
     */
    private String extractMetadata(Document doc) {
        String description = doc.select("meta[name=description]").attr("content");
        String keywords = doc.select("meta[name=keywords]").attr("content");
        StringBuilder metadata = new StringBuilder();
        if (!description.isEmpty()) {
            metadata.append("\"description\":\"").append(description.replace("\"", "\\\"")).append("\"");
        }
        if (!keywords.isEmpty()) {
            if (metadata.length() > 0) metadata.append(", ");
            metadata.append("\"keywords\":\"").append(keywords.replace("\"", "\\\"")).append("\"");
        }
        return "{" + metadata.toString() + "}";
    }

    /**
     * Verifică dacă un string este un URL valid.
     */
    private boolean isValidUrl(String url) {
        try {
            new URI(url).parseServerAuthority();
            return (url.startsWith("http://") || url.startsWith("https://"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Verifică dacă un URL se termină cu o extensie de fișier care trebuie exclusă.
     */
    private boolean isFileUrl(String url) {
        String lowerUrl = url.toLowerCase();
        return (currentConfig.isExcludeImages() && (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") || lowerUrl.endsWith(".png") || lowerUrl.endsWith(".gif"))) ||
                (currentConfig.isExcludePdfs() && lowerUrl.endsWith(".pdf"));
    }

    /**
     * Trimite actualizare prin WebSocket cu detalii despre ultimul URL.
     */
    private void sendCrawlerStatusUpdate(String lastProcessedUrl, Url.UrlStatus status) {
        long visitedCount = urlRepository.countByStatus(Url.UrlStatus.VISITED);
        long pendingCount = urlRepository.countByStatus(Url.UrlStatus.PENDING);
        long failedCount = urlRepository.countByStatus(Url.UrlStatus.FAILED);

        var statusUpdate = new java.util.HashMap<String, Object>();
        statusUpdate.put("isRunning", isRunning.get());
        statusUpdate.put("visitedCount", visitedCount);
        statusUpdate.put("pendingCount", pendingCount);
        statusUpdate.put("failedCount", failedCount);

        if (lastProcessedUrl != null && status != null) {
            statusUpdate.put("recentUrl", lastProcessedUrl);
            statusUpdate.put("recentStatus", status.toString());
        }

        messagingTemplate.convertAndSend("/topic/crawlerStatus", statusUpdate);
        log.debug("Trimis actualizare status crawler prin WebSocket.");
    }

    /**
     * Overload pentru apeluri simple (fără URL specific), ex: la start/stop.
     */
    public boolean isExcluded(String url) {
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".jpg") ||
                lowerUrl.endsWith(".jpeg") ||
                lowerUrl.endsWith(".png") ||
                lowerUrl.endsWith(".pdf") ||
                lowerUrl.endsWith(".gif") ||
                lowerUrl.endsWith(".zip");
    }

    public String getDomain(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            if (host == null) return null;

            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            String[] parts = host.split("\\.");
            if (parts.length > 2) {
                return parts[parts.length - 2] + "." + parts[parts.length - 1];
            }

            return host;
        } catch (Exception e) {
            return null;
        }
    }
    private void sendCrawlerStatusUpdate() {
        sendCrawlerStatusUpdate(null, null);
    }

    @PreDestroy
    public void shutdown() {
        stopCrawling();
        log.info("CrawlerService shutdown hook executat.");
    }


}