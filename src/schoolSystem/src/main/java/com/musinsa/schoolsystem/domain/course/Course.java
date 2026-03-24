package com.musinsa.schoolsystem.domain.course;

import com.musinsa.schoolsystem.domain.course.error.CourseErrorCode;
import com.musinsa.schoolsystem.domain.department.Department;
import com.musinsa.schoolsystem.domain.professor.Professor;
import com.musinsa.schoolsystem.global.exception.BusinessException;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "courses",
        indexes = {
                @Index(name = "idx_course_code", columnList = "code", unique = true),
                @Index(name = "idx_course_name", columnList = "name"),
                @Index(name = "idx_course_department", columnList = "department_id"),
                @Index(name = "idx_course_professor", columnList = "professor_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int enrolledCount;

    @Version
    private long version;

    @ElementCollection
    @CollectionTable(name = "course_schedule_slots", joinColumns = @JoinColumn(name = "course_id"))
    private List<CourseScheduleSlot> scheduleSlots = new ArrayList<>();

    public Course(
            String code,
            String name,
            Department department,
            Professor professor,
            int credits,
            int capacity,
            List<CourseScheduleSlot> scheduleSlots
    ) {
        this.code = code;
        this.name = name;
        this.department = department;
        this.professor = professor;
        this.credits = credits;
        this.capacity = capacity;
        this.enrolledCount = 0;
        this.scheduleSlots = new ArrayList<>(scheduleSlots);
    }

    public List<CourseScheduleSlot> getScheduleSlots() {
        return List.copyOf(scheduleSlots);
    }

    public boolean hasRemainingSeat() {
        return enrolledCount < capacity;
    }

    public void increaseEnrollment() {
        if (!hasRemainingSeat()) {
            throw new BusinessException(CourseErrorCode.COURSE_FULL);
        }
        enrolledCount += 1;
    }

    public void decreaseEnrollment() {
        if (enrolledCount <= 0) {
            throw new BusinessException(CourseErrorCode.COURSE_ENROLLMENT_EMPTY);
        }
        enrolledCount -= 1;
    }
}
