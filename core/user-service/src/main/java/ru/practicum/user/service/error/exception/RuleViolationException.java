package ru.practicum.user.service.error.exception;

public class RuleViolationException extends RuntimeException {
    public RuleViolationException(String massage) {
        super(massage);
    }
}
