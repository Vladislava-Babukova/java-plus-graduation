package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.comment.enums.Status;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentDto {
    @NotNull(message = "Status can't be null")
    private Status status;
}
