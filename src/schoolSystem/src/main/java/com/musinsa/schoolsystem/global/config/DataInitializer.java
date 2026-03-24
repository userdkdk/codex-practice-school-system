package com.musinsa.schoolsystem.global.config;

import com.musinsa.schoolsystem.domain.course.Course;
import com.musinsa.schoolsystem.domain.course.CourseRepository;
import com.musinsa.schoolsystem.domain.course.CourseScheduleSlot;
import com.musinsa.schoolsystem.domain.department.Department;
import com.musinsa.schoolsystem.domain.department.DepartmentRepository;
import com.musinsa.schoolsystem.domain.professor.Professor;
import com.musinsa.schoolsystem.domain.professor.ProfessorRepository;
import com.musinsa.schoolsystem.domain.student.Student;
import com.musinsa.schoolsystem.domain.student.StudentRepository;
import com.musinsa.schoolsystem.global.readiness.ApplicationReadinessState;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final String[] DEPARTMENT_NAMES = {
            "컴퓨터공학과", "전자공학과", "기계공학과", "산업공학과", "경영학과",
            "경제학과", "국어국문학과", "영어영문학과", "수학과", "화학과"
    };
    private static final String[] LAST_NAMES = {
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
            "한", "오", "서", "신", "권", "황", "안", "송", "류", "홍"
    };
    private static final String[] FIRST_NAME_PREFIXES = {
            "민", "서", "도", "예", "지", "하", "현", "유", "주", "태",
            "시", "가", "채", "은", "준", "승", "우", "아", "다", "원"
    };
    private static final String[] FIRST_NAME_SUFFIXES = {
            "준", "연", "윤", "은", "우", "진", "호", "민", "현", "아",
            "린", "서", "율", "빈", "찬", "영", "재", "경", "희", "솔"
    };
    private static final String[] COURSE_SUBJECTS = {
            "자료구조", "운영체제", "데이터베이스", "웹프로그래밍", "알고리즘", "컴퓨터네트워크", "인공지능개론",
            "회로이론", "마케팅원론", "미적분학", "일반화학", "확률통계", "소프트웨어공학", "객체지향프로그래밍",
            "분산시스템", "디지털논리", "재무관리", "선형대수", "유기화학", "기계설계", "서비스기획", "머신러닝"
    };
    private static final String[] COURSE_MODIFIERS = {
            "기초", "응용", "심화", "실습", "세미나", "프로젝트", "특강", "개론", "설계", "분석"
    };
    private static final String[] COURSE_FORMATS = {
            "A", "B", "1반", "2반", "주간", "야간", "집중", "캡스톤"
    };

    private final DepartmentRepository departmentRepository;
    private final ProfessorRepository professorRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ApplicationReadinessState readinessState;

    @Override
    public void run(ApplicationArguments args) {
        readinessState.markInitializationStarted();
        try {
            if (studentRepository.count() == 0) {
                List<Department> departments = departmentRepository.saveAll(createDepartments());
                List<Professor> professors = professorRepository.saveAll(createProfessors(departments));
                courseRepository.saveAll(createCourses(departments, professors));
                studentRepository.saveAll(createStudents(departments));
            }
        } finally {
            readinessState.markReady();
        }
    }

    private List<Department> createDepartments() {
        List<Department> departments = new ArrayList<>();
        for (String departmentName : DEPARTMENT_NAMES) {
            departments.add(new Department(departmentName));
        }
        return departments;
    }

    private List<Professor> createProfessors(List<Department> departments) {
        List<Professor> professors = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            professors.add(new Professor(fullName(i, 11), departments.get(i % departments.size())));
        }
        return professors;
    }

    private List<Course> createCourses(List<Department> departments, List<Professor> professors) {
        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Department department = departments.get(i % departments.size());
            Professor professor = professors.get(i % professors.size());
            courses.add(new Course(
                    "CRS-" + (1000 + i),
                    courseName(i),
                    department,
                    professor,
                    (i % 3) + 2,
                    20 + (i % 31),
                    List.of(scheduleOf(i))
            ));
        }
        return courses;
    }

    private List<Student> createStudents(List<Department> departments) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            students.add(new Student(
                    "2026" + String.format("%05d", i + 1),
                    fullName(i, 37),
                    departments.get(i % departments.size()),
                    (i % 4) + 1
            ));
        }
        return students;
    }

    private CourseScheduleSlot scheduleOf(int index) {
        DayOfWeek day = DayOfWeek.of((index % 5) + 1);
        int slot = index % 6;
        LocalTime start = LocalTime.of(9 + (slot * 2), 0);
        return new CourseScheduleSlot(day, start, start.plusMinutes(90));
    }

    private String fullName(int index, int salt) {
        String lastName = LAST_NAMES[(index + salt) % LAST_NAMES.length];
        String firstName = FIRST_NAME_PREFIXES[(index * 3 + salt) % FIRST_NAME_PREFIXES.length]
                + FIRST_NAME_SUFFIXES[(index * 7 + salt) % FIRST_NAME_SUFFIXES.length];
        return lastName + firstName;
    }

    private String courseName(int index) {
        return COURSE_SUBJECTS[index % COURSE_SUBJECTS.length]
                + " "
                + COURSE_MODIFIERS[(index / COURSE_SUBJECTS.length) % COURSE_MODIFIERS.length]
                + " "
                + COURSE_FORMATS[(index / (COURSE_SUBJECTS.length * 2)) % COURSE_FORMATS.length];
    }
}
