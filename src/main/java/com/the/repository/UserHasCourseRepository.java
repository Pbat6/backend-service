package com.the.repository;

import com.the.model.Course;
import com.the.model.User;
import com.the.model.UserHasCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHasCourseRepository extends JpaRepository<UserHasCourse, Long> {
    boolean existsByUserAndCourse(User user, Course course);
}
