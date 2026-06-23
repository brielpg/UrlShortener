package br.com.url_shortener.domain.exceptions;

public class UrlRequiredException extends RuntimeException {
    public UrlRequiredException(String message) {
        super(message);
    }
}
