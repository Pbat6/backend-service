package com.the.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;

@Getter
public class LessonRequestDTO {

    @NotBlank(message = "Title must not be blank")
    private String title;

    /**
     * Đường link video từ Google Drive.
     * Phải là một URL hợp lệ.
     */
    @NotBlank(message = "Google Drive link must not be blank")
    @URL(message = "Invalid URL format for Google Drive link")
    private String googleDriveLink;

    @NotNull(message = "Lesson order must not be null")
    @Min(value = 1, message = "Lesson order must be at least 1")
    private Integer lessonOrder;
}
