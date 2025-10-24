package com.the.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CourseDetailsDTO {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String authorName;

    private List<LessonResponseDTO> lessons;
}
