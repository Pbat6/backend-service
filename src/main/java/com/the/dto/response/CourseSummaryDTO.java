package com.the.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseSummaryDTO {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private String authorName; // Hiển thị tên tác giả thay vì cả object User
}
