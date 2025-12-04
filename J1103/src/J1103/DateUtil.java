package J1103;

import java.time.LocalDate;
import java.time.Clock;
import java.time.ZoneId;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 시스템 시간 기반 날짜 유틸리티 클래스.
 * 시스템 시간이 변경되어도 항상 최신 시간을 반영합니다.
 * JVM의 TimeZone 캐시를 우회하여 실시간 시스템 시간을 사용합니다.
 */
public class DateUtil {
    
    /**
     * 현재 시스템의 오늘 날짜를 반환합니다.
     * JVM TimeZone 캐시를 갱신하여 시스템 시간 변경을 즉시 반영합니다.
     * @return 오늘 날짜 (LocalDate)
     */
    public static LocalDate getToday() {
        // JVM의 TimeZone 캐시를 강제로 갱신
        TimeZone.setDefault(null);
        
        // 시스템 기본 시간대를 새로 조회하여 Clock 생성
        ZoneId zoneId = ZoneId.systemDefault();
        Clock clock = Clock.system(zoneId);
        
        // 갱신된 Clock으로 현재 날짜 반환
        return LocalDate.now(clock);
    }
    
    /**
     * 오늘의 요일을 반환합니다.
     * @return 오늘의 DayOfWeek
     */
    public static DayOfWeek getTodayDayOfWeek() {
        return getToday().getDayOfWeek();
    }
    
    /**
     * 오늘의 한국어 요일명을 반환합니다 (예: "월", "화").
     * @return 한국어 요일 문자열
     */
    public static String getTodayKoreanDay() {
        return getTodayDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREA);
    }
    
    /**
     * 주어진 날짜가 오늘인지 확인합니다.
     * @param date 확인할 날짜
     * @return 오늘이면 true
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(getToday());
    }
}
