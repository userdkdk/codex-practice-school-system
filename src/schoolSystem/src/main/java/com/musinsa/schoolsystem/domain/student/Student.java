package com.musinsa.schoolsystem.domain.student;

import com.musinsa.schoolsystem.domain.department.Department;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "students",
        indexes = {
                @Index(name = "idx_student_number", columnList = "student_number", unique = true),
                @Index(name = "idx_student_name", columnList = "name"),
                @Index(name = "idx_student_department", columnList = "department_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String studentNumber;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private int grade;

    public Student(String studentNumber, String name, Department department, int grade) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.department = department;
        this.grade = grade;
    }
}
