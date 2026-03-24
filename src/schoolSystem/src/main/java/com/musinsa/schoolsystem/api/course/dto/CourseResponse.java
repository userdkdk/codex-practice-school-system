package com.musinsa.schoolsystem.api.course.dto;

import com.musinsa.schoolsystem.domain.course.Course;
import com.musinsa.schoolsystem.domain.course.CourseScheduleSlot;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.stream.Collectors;

@Schema(description = "강좌 조회 응답")
public record CourseResponse(
        Long id,
        String code,
        String name,
        String departmentName,
        String professorName,
        int credits,
        int capacity,
        int enrolled,
        String schedule
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getDepartment().getName(),
                course.getProfessor().getName(),
                course.getCredits(),
                course.getCapacity(),
                course.getEnrolledCount(),
                formatSchedule(course)
        );
    }

    private static String formatSchedule(Course course) {
        return course.getScheduleSlots().stream()
                .map(CourseResponse::formatSlot)
                .collect(Collectors.joining(", "));
    }

    private static String formatSlot(CourseScheduleSlot slot) {
        return slot.getDayOfWeek() + " " + slot.getStartTime() + "-" + slot.getEndTime();
    }
}
