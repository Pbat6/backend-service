package com.the.service.impl;

import com.the.dto.request.CourseRequestDTO;
import com.the.dto.request.LessonRequestDTO;
import com.the.dto.response.CourseSummaryDTO;
import com.the.exception.InvalidDataException;
import com.the.exception.ResourceNotFoundException;
import com.the.model.Course;
import com.the.model.Lesson;
import com.the.model.User;
import com.the.model.UserHasCourse;
import com.the.repository.CourseRepository;
import com.the.repository.LessonRepository;
import com.the.repository.UserHasCourseRepository;
import com.the.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional // Áp dụng transaction cho tất cả các method public trong class
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserHasCourseRepository userEnrollmentRepository;

    @Override
    public Course createCourse(CourseRequestDTO courseDTO, User author) {
        Course course = Course.builder()
                .title(courseDTO.getTitle())
                .description(courseDTO.getDescription())
                .thumbnailUrl(courseDTO.getThumbnailUrl())
                .author(author)
                .build();
        // Lưu và trả về đối tượng Course đã được tạo
        return courseRepository.save(course);
    }

    @Override
    public Lesson addLesson(Long courseId, LessonRequestDTO lessonDTO) {
        // Tìm khóa học, nếu không có sẽ ném ra exception
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        Lesson lesson = Lesson.builder()
                .title(lessonDTO.getTitle())
                .googleDriveLink(lessonDTO.getGoogleDriveLink())
                .lessonOrder(lessonDTO.getLessonOrder())
                .course(course)
                .build();

        // Lưu và trả về đối tượng Lesson đã được tạo
        return lessonRepository.save(lesson);
    }

    @Override
    @Transactional(readOnly = true) // Tối ưu cho các truy vấn chỉ đọc
    public Page<CourseSummaryDTO> getAllCourses(Pageable pageable) {
//        return courseRepository.findAll(pageable);
        return null;
    }


    @Override
    @Transactional(readOnly = true)
    public Course getCourseDetails(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));
    }

    @Override
    public void enrollToCourse(Long courseId, User user) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Kiểm tra xem người dùng đã đăng ký khóa học này chưa để tránh trùng lặp
        if (userEnrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new InvalidDataException("User has already enrolled in this course.");
        }

        UserHasCourse enrollment = UserHasCourse.builder()
                .user(user)
                .course(course)
                .build();

        userEnrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> getMyCourse(Pageable pageable, User actor) {
        return courseRepository.findCoursesByEnrolledUser(actor.getId(), pageable);
    }
}
