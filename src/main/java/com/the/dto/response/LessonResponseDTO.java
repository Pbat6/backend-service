package com.the.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LessonResponseDTO {
    private Long id;
    private String title;
    private int lessonOrder;

    /**
     * Chỉ bao gồm trường này trong JSON response nếu giá trị của nó không null.
     * Dùng để ẩn link video với người dùng chưa đăng ký khóa học.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String googleDriveLink;
}
