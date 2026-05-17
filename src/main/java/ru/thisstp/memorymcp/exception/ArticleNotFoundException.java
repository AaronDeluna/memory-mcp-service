package ru.thisstp.memorymcp.exception;

public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(Long id) {
        super("Article " + id + " not found");
    }
}
