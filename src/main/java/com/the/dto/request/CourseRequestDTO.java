package com.the.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CourseRequestDTO {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String description;

    private String thumbnailUrl;
}
