package ru.thisstp.memorymcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.thisstp.memorymcp.entity.Article;
import ru.thisstp.memorymcp.exception.ArticleNotFoundException;
import ru.thisstp.memorymcp.repository.ArticleRepository;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Article getById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    public Page<Article> list(String title, Pageable pageable) {
        return title == null || title.isBlank()
                ? articleRepository.findAll(pageable)
                : articleRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
}
