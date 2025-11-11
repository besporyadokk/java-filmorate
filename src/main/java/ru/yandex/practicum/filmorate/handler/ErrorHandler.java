package ru.yandex.practicum.filmorate.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.FriendAddingException;
import ru.yandex.practicum.filmorate.exception.LikesAddingException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;


import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {
    private final String error = "error";
    private final String message = "message";

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFoundHandler(NotFoundException e) {
        return Map.of(error, "Объект не найден.",
                message, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validationExceptionHandler(ValidationException e) {
        return Map.of(error, "Ошибка валидации.",
                message, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> friendsAddingExceptionHandler(FriendAddingException e) {
        return Map.of(error, "Ошибка добавления друга",
                message, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> likesSendingExceptionHandler(LikesAddingException e) {
        return Map.of(error, "Ошибка добавления лайка",
                message, e.getMessage());
    }
}