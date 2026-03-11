package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@RestControllerAdvice
@Slf4j
public class ExceptionHandler {
    /*
     Класс для логирования исключений
     и для возвращения более полных текстов ошибок
     и более подходящих кодов ответов.
    */

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка валидации: невалидные данные DTO",
                errorMessage
        );
        log.warn("Ошибка валидации DTO: {}", errorMessage);
        return errorResponse;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleConstraintViolation(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));

        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка валидации: невалидный параметр",
                errorMessage
        );
        log.warn("Ошибка валидации параметра: {}", errorMessage);
        return errorResponse;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String errorMessage = e.getAllValidationResults().stream()
                .flatMap(validationResult -> validationResult.getResolvableErrors().stream())
                .map(this::extractErrorMessage)
                .filter(msg -> msg != null && !msg.isEmpty())
                .collect(Collectors.joining("; "));

        // Если не удалось извлечь сообщения, используем общее описание
        if (errorMessage.isEmpty()) {
            errorMessage = "Ошибка валидации входных данных";
        }

        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка валидации входных данных",
                errorMessage
        );
        log.warn("Ошибка валидации входных данных: {}", errorMessage);
        return errorResponse;
    }

    // Вспомогательный метод для извлечения сообщения из MessageSourceResolvable
    private String extractErrorMessage(MessageSourceResolvable resolvable) {
        // Пытаемся получить default message
        if (resolvable.getDefaultMessage() != null) {
            return resolvable.getDefaultMessage();
        }

        // Для FieldError пытаемся получить field-specific message
        if (resolvable instanceof FieldError fieldError) {
            return fieldError.getDefaultMessage();
        }

        // В крайнем случае возвращаем toString()
        return resolvable.toString();
    }


    @org.springframework.web.bind.annotation.ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleMissingParameters(Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : "Отсутствует обязательный параметр";

        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка валидации: отсутствует обязательный параметр",
                errorMessage
        );
        log.warn("Отсутствует обязательный параметр: {}", errorMessage);
        return errorResponse;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleAlreadyExists(AlreadyExistException e) {
        ExceptionResponse errorResponse = new ExceptionResponse("Ошибка конфликта", e.getMessage());
        log.warn("{}", errorResponse);
        return errorResponse;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({NoSuchElementException.class, NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNotFound(RuntimeException e) {
        ExceptionResponse errorResponse = new ExceptionResponse("Не найдено", e.getMessage());
        log.warn("{}", errorResponse);
        return errorResponse;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({PermissionException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handlePermission(RuntimeException e) {
        ExceptionResponse errorResponse = new ExceptionResponse("Ошибка прав доступа", e.getMessage());
        log.warn("{}", errorResponse);
        return errorResponse;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleOtherExceptions(Exception e) {
        ExceptionResponse errorResponse = new ExceptionResponse("Внутренняя ошибка сервера", e.getMessage());
        log.error("Произошла непредвиденная ошибка", e);
        return errorResponse;
    }
}