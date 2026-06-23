package br.com.url_shortener.infrastructure.handlers;

import br.com.url_shortener.application.dtos.ErrorDto;
import br.com.url_shortener.domain.exceptions.UrlNotFoundException;
import br.com.url_shortener.domain.exceptions.UrlRequiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDto> handleUrlNotFoundException(Exception ex) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ErrorDto dto = new ErrorDto(httpStatus.value(), httpStatus.name(), ex.getMessage());
        return ResponseEntity.status(httpStatus).body(dto);
    }

    @ExceptionHandler(UrlRequiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDto> handleUrlRequiredException(Exception ex) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ErrorDto dto = new ErrorDto(httpStatus.value(), httpStatus.name(), ex.getMessage());
        return ResponseEntity.status(httpStatus).body(dto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ErrorDto errorDto = new ErrorDto(httpStatus.value(), httpStatus.name(), ex.getMessage());
        return ResponseEntity.status(httpStatus).body(errorDto);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorDto> handleGeneralException(Exception ex) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorDto errorDto = new ErrorDto(httpStatus.value(), httpStatus.name(), ex.getMessage());
        return ResponseEntity.status(httpStatus).body(errorDto);
    }
}
