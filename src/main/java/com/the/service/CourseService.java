package com.the.service;

import com.the.dto.request.CourseRequestDTO;
import com.the.dto.request.LessonRequestDTO;
import com.the.dto.response.CourseSummaryDTO;
import com.the.model.Course;
import com.the.model.Lesson;
import com.the.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface CourseService {
    Course createCourse(CourseRequestDTO courseDTO, User author);
    Lesson addLesson(Long courseId, LessonRequestDTO lessonDTO);
    Page<CourseSummaryDTO> getAllCourses(Pageable pageable);
    Course getCourseDetails(Long courseId);
    void enrollToCourse(Long courseId, User user);
    Page<Course> getMyCourse(Pageable pageable, User actor);
}
