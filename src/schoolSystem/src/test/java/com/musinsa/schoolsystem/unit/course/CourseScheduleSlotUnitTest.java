package com.musinsa.schoolsystem.unit.course;

import static org.assertj.core.api.Assertions.assertThat;

import com.musinsa.schoolsystem.domain.course.CourseScheduleSlot;
import java.time.DayOfWeek;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CourseScheduleSlotUnitTest {

    @Test
    @DisplayName("같은 요일에서 시간이 겹치면 시간표 충돌로 판단한다")
    // 체크리스트: 4. 수강신청 규칙 > 시간표 충돌 신청이 차단된다.
    // 체크리스트: 7. 테스트 > 시간 충돌 실패 테스트가 있다.
    void overlapsReturnsTrueWhenTimeRangesIntersectOnSameDay() {
        CourseScheduleSlot first = new CourseScheduleSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 30));
        CourseScheduleSlot second = new CourseScheduleSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 30));

        assertThat(first.overlaps(second)).isTrue();
    }

    @Test
    @DisplayName("요일이 다르면 시간표 충돌이 아니다")
    // 체크리스트: 4. 수강신청 규칙 > 시간표 충돌 신청이 차단된다.
    void overlapsReturnsFalseWhenDaysAreDifferent() {
        CourseScheduleSlot first = new CourseScheduleSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 30));
        CourseScheduleSlot second = new CourseScheduleSlot(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(10, 30));

        assertThat(first.overlaps(second)).isFalse();
    }

    @Test
    @DisplayName("시간이 맞닿기만 하면 시간표 충돌이 아니다")
    // 체크리스트: 4. 수강신청 규칙 > 시간표 충돌 신청이 차단된다.
    void overlapsReturnsFalseWhenTimeRangesDoNotIntersect() {
        CourseScheduleSlot first = new CourseScheduleSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 30));
        CourseScheduleSlot second = new CourseScheduleSlot(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(12, 0));

        assertThat(first.overlaps(second)).isFalse();
    }
}
