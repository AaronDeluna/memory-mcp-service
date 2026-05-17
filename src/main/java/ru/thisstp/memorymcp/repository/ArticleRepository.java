package ru.thisstp.memorymcp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.thisstp.memorymcp.entity.Article;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findByUrl(String url);

    Optional<Article> findFirstByTextHash(String textHash);

    boolean existsByTextHash(String textHash);

    Page<Article> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("select a.url from Article a")
    List<String> findAllUrls();

    @Query("select a.textHash from Article a")
    List<String> findAllTextHashes();

    @Query(value = "SELECT coalesce(max(CAST(regexp_replace(url, '.*/articles/(\\d+)/?$', '\\1') AS BIGINT)), 0) FROM articles",
            nativeQuery = true)
    long findMaxHabrId();
}
