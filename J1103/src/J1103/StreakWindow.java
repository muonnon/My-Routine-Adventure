package J1103;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Set;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth; 
import java.util.Locale; 

/**
 * ⭐ [수정] Player의 루틴 연속 달성 현황(스트릭)을 달력 형태로 표시하는 패널을 제공하는 클래스입니다.
 * (더 이상 JFrame이 아니므로 serialVersionUID는 불필요합니다.)
 */
public class StreakWindow { 

    private final Player player;
    private final JPanel mainPanel; // 최종적으로 반환할 메인 패널
    private final JLabel currentMonthLabel; 
    private final JPanel calendarPanel; 
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월", Locale.KOREAN);
    private YearMonth currentYearMonth; 

    public StreakWindow(Player player) { 
        this.player = player;
        this.currentYearMonth = YearMonth.now();
        
        this.mainPanel = new JPanel(new BorderLayout(10, 10)); // 여백 추가
        this.currentMonthLabel = new JLabel("", JLabel.CENTER);
        this.calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5)); // 7일 그리드
        
        initUI();
    }
    
    /**
     * ⭐ [신규] MainDashboard의 탭에 삽입될 UI 패널을 반환합니다.
     */
    public JComponent getUI() {
        return mainPanel;
    }
    
    private void initUI() {
        // 1. 월 이동 버튼 및 현재 월 표시 패널
        JPanel navigationPanel = new JPanel(new BorderLayout());
        
        JButton prevButton = new JButton("◀");
        prevButton.addActionListener(e -> navigateMonth(-1));
        
        JButton nextButton = new JButton("▶");
        nextButton.addActionListener(e -> navigateMonth(1));
        
        navigationPanel.add(prevButton, BorderLayout.WEST);
        navigationPanel.add(currentMonthLabel, BorderLayout.CENTER);
        navigationPanel.add(nextButton, BorderLayout.EAST);
        
        // 2. 요일 헤더 패널 (일, 월, 화...)
        JPanel dayHeaderPanel = createDaysOfWeekHeader();
        
        // 3. 달력 컨테이너 (요일 헤더와 날짜 패널을 포함)
        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.add(dayHeaderPanel, BorderLayout.NORTH); // 요일 헤더
        calendarContainer.add(calendarPanel, BorderLayout.CENTER); // 날짜 그리드
        
        mainPanel.add(navigationPanel, BorderLayout.NORTH);
        mainPanel.add(calendarContainer, BorderLayout.CENTER);
        
        updateCalendarUI(); // 초기 데이터 로드
    }
    
    /**
     * 월요일부터 일요일까지의 요일 헤더를 생성합니다.
     */
    private JPanel createDaysOfWeekHeader() {
        JPanel header = new JPanel(new GridLayout(1, 7, 5, 5));
        String[] dayNames = {"일", "월", "화", "수", "목", "금", "토"};
        
        for (int i = 0; i < dayNames.length; i++) {
            JLabel label = new JLabel(dayNames[i], JLabel.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            if (i == 0) { // 일요일
                label.setForeground(Color.RED);
            } else if (i == 6) { // 토요일
                label.setForeground(Color.BLUE);
            }
            header.add(label);
        }
        return header;
    }
    
    private void navigateMonth(int amount) {
        this.currentYearMonth = this.currentYearMonth.plusMonths(amount);
        updateCalendarUI();
    }
    
    /**
     * 현재 연/월에 해당하는 달력 UI를 갱신합니다.
     * 루틴 완료 날짜(스트릭)를 표시합니다.
     */
    public void updateCalendarUI() {
        currentMonthLabel.setText(currentYearMonth.format(MONTH_YEAR_FORMATTER));
        calendarPanel.removeAll();
        
        // 1. 달력 시작 요일을 맞추기 위한 빈 칸 추가
        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue(); // 1=월요일, 7=일요일
        
        // Java DayOfWeek (월=1 ~ 일=7)을 달력 그리드 (일=0 ~ 토=6)에 맞추기 위한 오프셋 계산
        int offset = dayOfWeekValue % 7; 
        
        for (int i = 0; i < offset; i++) {
            calendarPanel.add(new JLabel("")); // 빈 라벨 추가
        }
        
        // 2. 현재 월의 날짜 채우기
        int daysInMonth = currentYearMonth.lengthOfMonth();
        Set<LocalDate> streakDates = player.getStreakDates(); 

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            JLabel dayLabel = new JLabel(String.valueOf(day), JLabel.CENTER);
            dayLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            dayLabel.setOpaque(true);

            // 스트릭 날짜 확인
            if (streakDates.contains(date)) {
                dayLabel.setBackground(new Color(0, 128, 0)); // 진한 녹색 (완료)
                dayLabel.setForeground(Color.WHITE); 
                dayLabel.setToolTipText("루틴 완료!");
            } else {
                 dayLabel.setBackground(Color.WHITE); // 배경 흰색 (미완료)
            }
            
            // 오늘 날짜 표시 (강조)
            if (date.equals(LocalDate.now())) {
                 dayLabel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
            }
            
            // 주말 색상
            if (date.getDayOfWeek().getValue() == 6) { // 토요일 (6)
                 dayLabel.setForeground(Color.BLUE);
            } else if (date.getDayOfWeek().getValue() == 7) { // 일요일 (7)
                 dayLabel.setForeground(Color.RED);
            }
            
            calendarPanel.add(dayLabel);
        }
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}