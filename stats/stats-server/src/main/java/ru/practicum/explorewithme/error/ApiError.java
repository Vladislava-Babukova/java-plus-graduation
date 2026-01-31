package ru.practicum.explorewithme.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ApiError {

    private HttpStatus status;
    private String message; // Сообщение для пользователя
    private String reason; // Сообщение для разработчика
    private String stackTrace; // Не для production

}
