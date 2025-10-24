package com.the.controller;

import com.the.dto.request.CourseRequestDTO;
import com.the.dto.request.LessonRequestDTO;
import com.the.dto.response.CourseSummaryDTO;
import com.the.dto.response.LessonResponseDTO;
import com.the.dto.response.ResponseData;
import com.the.model.Course;
import com.the.model.User;
import com.the.repository.UserHasCourseRepository;
import com.the.service.CourseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
@Tag(name = "Course Controller")
public class CourseController {
    private final CourseService courseService;
    private final UserHasCourseRepository userHasCourseRepository;


    @PostMapping("/")
    @PreAuthorize("hasAuthority('course:create')")
    public ResponseData<ResponseData<CourseSummaryDTO>> createCourse(@RequestBody @Valid CourseRequestDTO courseRequestDTO) {
        User author = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        courseService.createCourse(courseRequestDTO, author);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Course created successfully");
    }

    @PostMapping("/{courseId}/lessons")
    @PreAuthorize("hasAuthority('course:update')")
    public ResponseData<LessonResponseDTO> addLesson(@PathVariable Long courseId, @RequestBody @Valid LessonRequestDTO lessonRequestDTO) {
        courseService.addLesson(courseId, lessonRequestDTO);
        return new ResponseData<>(HttpStatus.CREATED.value(), "Lesson added successfully");
    }

    @GetMapping
    @PreAuthorize("hasAuthority('course:view')")
    public ResponseData<Page<CourseSummaryDTO>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<CourseSummaryDTO> courses = courseService.getAllCourses(pageable);
        return new ResponseData<>(HttpStatus.OK.value(), "Courses retrieved successfully", courses);
    }

    @GetMapping("/{courseId}")
    @PreAuthorize("hasAuthority('course:view')")
    public ResponseData<Course> getCourseDetails(@PathVariable Long courseId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Course course = courseService.getCourseDetails(courseId);
        // Kiểm tra xem user hiện tại đã đăng ký khóa học này chưa
        boolean isEnrolled = userHasCourseRepository.existsByUserAndCourse(user, course);
        return new ResponseData<>(HttpStatus.OK.value(), "Course details retrieved successfully", course);
    }

    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasAuthority('course:enroll')")
    public ResponseData<String> enrollToCourse(@PathVariable Long courseId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        courseService.enrollToCourse(courseId, user);
        return new ResponseData<>(HttpStatus.OK.value(), "Enrolled to course successfully");
    }

    @GetMapping("/my-courses")
    @PreAuthorize("isAuthenticated()")
    public ResponseData<Page<Course>> getMyCourse(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "id") String sortBy) {
        User actor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Course> courses = courseService.getMyCourse(pageable, actor);
        return new ResponseData<>(HttpStatus.OK.value(), "Course details retrieved successfully", courses);
    }

//    private CourseSummaryDTO mapToCourseSummaryDTO(Course course) {
//        return CourseSummaryDTO.builder()
//                .id(course.getId())
//                .title(course.getTitle())
//                .thumbnailUrl(course.getThumbnailUrl())
//                .authorName(course.getAuthor().getFirstName() + " " + course.getAuthor().getLastName())
//                .build();
//    }
//
//    private CourseDetailsDTO mapToCourseDetailsDTO(Course course, boolean isEnrolled) {
//        return CourseDetailsDTO.builder()
//                .id(course.getId())
//                .title(course.getTitle())
//                .description(course.getDescription())
//                .thumbnailUrl(course.getThumbnailUrl())
//                .authorName(course.getAuthor().getFirstName() + " " + course.getAuthor().getLastName())
//                .lessons(course.getLessons().stream()
//                        .map(lesson -> mapToLessonDTO(lesson, isEnrolled))
//                        .collect(Collectors.toList()))
//                .build();
//    }
//
//    private LessonDTO mapToLessonDTO(Lesson lesson, boolean includeLink) {
//        String videoLink = includeLink ? "https://drive.google.com/uc?id=" + extractGoogleDriveFileId(lesson.getGoogleDriveLink()) : null;
//
//        return LessonDTO.builder()
//                .id(lesson.getId())
//                .title(lesson.getTitle())
//                .lessonOrder(lesson.getLessonOrder())
//                .googleDriveLink(videoLink)
//                .build();
//    }
//
//    private String extractGoogleDriveFileId(String url) {
//        // Trích xuất ID từ link Google Drive (ví dụ: .../d/FILE_ID/view)
//        int dIndex = url.indexOf("/d/");
//        if (dIndex != -1) {
//            int viewIndex = url.indexOf("/view", dIndex);
//            if (viewIndex != -1) {
//                return url.substring(dIndex + 3, viewIndex);
//            }
//        }
//        // Trả về chuỗi rỗng nếu không tìm thấy ID
//        return "";
//    }
}
