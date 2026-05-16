package ru.thisstp.memorymcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.thisstp.memorymcp.entity.Article;
import ru.thisstp.memorymcp.repository.ArticleRepository;
import ru.thisstp.memorymcp.util.HtmlToMarkdown;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class HabrParserService {

    private static final String API_URL = "https://habr.com/kek/v2/articles/";
    private static final String ARTICLE_URL = "https://habr.com/ru/articles/";

    private static final long MAX_ID = 210_860L;
    private static final int BATCH_SIZE = 100;

    private final ArticleRepository articleRepository;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final AtomicBoolean parseAllRunning = new AtomicBoolean(false);

    public Article parse(String id) {
        String url = ARTICLE_URL + id + "/";

        return articleRepository.findByUrl(url)
                .orElseGet(() -> articleRepository.save(fetch(id, url)));
    }

    @Async
    public void parseAll() {
        if (!parseAllRunning.compareAndSet(false, true)) {
            log.warn("parseAll is already running, skipping new request");
            return;
        }
        try {
            runParseAll();
        } finally {
            parseAllRunning.set(false);
        }
    }

    private void runParseAll() {
        log.info("parseAll started: maxId={}, batchSize={}", MAX_ID, BATCH_SIZE);

        Set<String> existingUrls = new HashSet<>(articleRepository.findAllUrls());
        log.info("Loaded {} existing urls — they will be skipped", existingUrls.size());

        List<Article> buffer = new ArrayList<>(BATCH_SIZE);
        int saved = 0;
        int skipped = 0;
        int failed = 0;
        long startMs = System.currentTimeMillis();

        for (long id = 1; id <= MAX_ID; id++) {
            String url = ARTICLE_URL + id + "/";
            if (existingUrls.contains(url)) {
                skipped++;
                continue;
            }
            try {
                Article article = fetch(String.valueOf(id), url);
                if (article.getTitle() == null || article.getTitle().isBlank()) {
                    failed++;
                    log.debug("Empty article id={}, skipping", id);
                    continue;
                }
                buffer.add(article);
                log.info("[{}/{}] fetched id={} title=\"{}\"",
                        id, MAX_ID, id, shortTitle(article.getTitle()));

                if (buffer.size() >= BATCH_SIZE) {
                    articleRepository.saveAll(buffer);
                    saved += buffer.size();
                    buffer.clear();

                    long elapsedSec = Math.max(1, (System.currentTimeMillis() - startMs) / 1000);
                    double rate = (double) (saved + failed) / elapsedSec;
                    long remainingSec = rate > 0 ? (long) ((MAX_ID - id) / rate) : -1;
                    log.info(">>> batch saved: total saved={}, skipped={}, failed={}, lastId={}, rate={} art/s, eta={}",
                            saved, skipped, failed, id, String.format("%.1f", rate), formatDuration(remainingSec));
                }
            } catch (Exception e) {
                failed++;
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                log.warn("Failed id={}: {} — {}", id, cause.getClass().getSimpleName(), cause.getMessage());
            }
        }

        if (!buffer.isEmpty()) {
            articleRepository.saveAll(buffer);
            saved += buffer.size();
            log.info(">>> final batch saved: {} articles", buffer.size());
        }
        long totalSec = (System.currentTimeMillis() - startMs) / 1000;
        log.info("parseAll finished in {}: saved={}, skipped={}, failed={}",
                formatDuration(totalSec), saved, skipped, failed);
    }

    private static String shortTitle(String title) {
        String t = title.replaceAll("\\s+", " ").strip();
        return t.length() > 80 ? t.substring(0, 80) + "…" : t;
    }

    private static String formatDuration(long seconds) {
        if (seconds < 0) return "?";
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%dh %dm %ds", h, m, s);
    }

    private Article fetch(String id, String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + id))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            if (status != 200) {
                throw new RuntimeException("HTTP " + status);
            }
            JsonNode root = objectMapper.readTree(resp.body());

            String markdown = HtmlToMarkdown.convert(root.path("textHtml").asText(""));

            return Article.builder()
                    .lang(root.path("lang").asText())
                    .title(root.path("titleHtml").asText())
                    .text(markdown)
                    .url(url)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("article " + id + ": " + e.getMessage(), e);
        }
    }
}
