package com.musinsa.schoolsystem.api.enrollment.dto;

import com.musinsa.schoolsystem.domain.course.Course;
import com.musinsa.schoolsystem.domain.course.CourseScheduleSlot;
import java.util.stream.Collectors;

public record TimetableCourseResponse(
        Long courseId,
        String courseCode,
        String courseName,
        int credits,
        String professorName,
        String schedule
) {
    public static TimetableCourseResponse from(Course course) {
        return new TimetableCourseResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getCredits(),
                course.getProfessor().getName(),
                course.getScheduleSlots().stream()
                        .map(TimetableCourseResponse::format)
                        .collect(Collectors.joining(", "))
        );
    }

    private static String format(CourseScheduleSlot slot) {
        return slot.getDayOfWeek() + " " + slot.getStartTime() + "-" + slot.getEndTime();
    }
}
