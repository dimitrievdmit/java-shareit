package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@SuppressWarnings("unused")
@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    /*
     Класс для логирования исключений
     и для возвращения более полных текстов ошибок
     и более подходящих кодов ответов.
    */

    @ExceptionHandler(LogicException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleLogicException(LogicException e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка логики",
                e.getMessage()
        );
        log.error("Ошибка логики: {}", e.getMessage());
        return errorResponse;
    }

    @ExceptionHandler(AlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleAlreadyExists(AlreadyExistException e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка конфликта",
                e.getMessage()
        );
        log.error("{}", errorResponse);
        return errorResponse;
    }

    @ExceptionHandler({NoSuchElementException.class, NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNotFound(RuntimeException e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
                "Не найдено",
                e.getMessage()
        );
        log.error("{}", errorResponse);
        return errorResponse;
    }

    @ExceptionHandler(PermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handlePermission(RuntimeException e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка прав доступа",
                e.getMessage()
        );
        log.error("{}", errorResponse);
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleOtherExceptions(Exception e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
                "Внутренняя ошибка сервера",
                e.getMessage()
        );
        log.error("Произошла непредвиденная ошибка", e);
        return errorResponse;
    }
}