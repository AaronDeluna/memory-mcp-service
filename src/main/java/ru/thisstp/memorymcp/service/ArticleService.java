package ru.thisstp.memorymcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.thisstp.memorymcp.entity.Article;
import ru.thisstp.memorymcp.repository.ArticleRepository;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Article getById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Article " + id + " not found"));
    }

    public Page<Article> list(String title, Pageable pageable) {
        return title == null || title.isBlank()
                ? articleRepository.findAll(pageable)
                : articleRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
}
