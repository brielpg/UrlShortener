package br.com.url_shortener.domain.exceptions;

public class ShortCodeRequiredException extends RuntimeException {
    public ShortCodeRequiredException(String message) {
        super(message);
    }
}
