package J1103;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatisticsPanel extends JPanel {

    private final Player player;
    private final RoutineManager manager;

    // UI 컴포넌트
    private JLabel monthTitleLabel;
    private JLabel countLabel;
    private JProgressBar monthProgressBar;
    private JLabel messageLabel; // ⭐ 격려 메시지 (유지!)
    private JTextArea statsDetailsArea;
    private SimpleBarChartPanel barChartPanel; // ⭐ 막대그래프 (추가)

    // 생성자 (manager만 받음)
    public StatisticsPanel(RoutineManager manager) {
        this.manager = manager;
        this.player = manager.getPlayer();
        
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initUI();
    }

    private void initUI() {
        // =================================================================
        // 1. 상단 (North): 제목, 횟수, 진행률 바
        // =================================================================
        JPanel topContainer = new JPanel(new GridLayout(3, 1, 5, 5));
        
        // 1-1. 월 제목
        monthTitleLabel = new JLabel("", JLabel.CENTER);
        monthTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        
        // 1-2. 달성 일수 표시
        countLabel = new JLabel("", JLabel.CENTER);
        countLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        
        // 1-3. 진행률 바
        monthProgressBar = new JProgressBar(0, 100);
        monthProgressBar.setStringPainted(true);
        monthProgressBar.setForeground(new Color(50, 205, 50)); // 라임 그린
        monthProgressBar.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        
        topContainer.add(monthTitleLabel);
        topContainer.add(countLabel);
        topContainer.add(monthProgressBar);
        
        add(topContainer, BorderLayout.NORTH);

        // =================================================================
        // 2. 중앙 (Center): 막대 그래프 (가장 넓은 공간 차지)
        // =================================================================
        barChartPanel = new SimpleBarChartPanel();
        add(barChartPanel, BorderLayout.CENTER);

        // =================================================================
        // 3. 하단 (South): 격려 메시지 + 상세 분석 텍스트
        // =================================================================
        JPanel bottomContainer = new JPanel(new BorderLayout(10, 10));
        
        // 3-1. 격려 메시지 패널 (구분선 + 메시지)
        JPanel messagePanel = new JPanel(new GridLayout(2, 1));
        messagePanel.add(new JLabel("▼ 오늘의 한마디 ▼", JLabel.CENTER)); // 구분선 유지
        
        messageLabel = new JLabel("", JLabel.CENTER); // ⭐ 격려 메시지 라벨 유지
        messageLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        messageLabel.setForeground(Color.BLUE);
        messagePanel.add(messageLabel);
        
        bottomContainer.add(messagePanel, BorderLayout.NORTH);

        // 3-2. 상세 분석 텍스트
        statsDetailsArea = new JTextArea(5, 20);
        statsDetailsArea.setEditable(false);
        statsDetailsArea.setBackground(new Color(245, 245, 245));
        statsDetailsArea.setBorder(BorderFactory.createTitledBorder("상세 분석"));
        
        bottomContainer.add(new JScrollPane(statsDetailsArea), BorderLayout.CENTER);
        
        add(bottomContainer, BorderLayout.SOUTH);

        // 초기 데이터 로드
        updateStatistics();
    }

    public void updateStatistics() {
        YearMonth currentYearMonth = YearMonth.now();
        int daysInMonth = currentYearMonth.lengthOfMonth();
        
        // 1. 월간 달성률 계산
        Set<LocalDate> streaks = player.getStreakDates();
        long activeDays = streaks.stream()
                .filter(date -> YearMonth.from(date).equals(currentYearMonth))
                .count();

        // 상단 텍스트 갱신
        monthTitleLabel.setText(currentYearMonth.getYear() + "년 " + currentYearMonth.getMonthValue() + "월 리포트");
        countLabel.setText(String.format("이번 달 총 %d일 루틴을 수행했습니다!", activeDays));
        
        int percentage = (int) ((activeDays / (double) daysInMonth) * 100);
        monthProgressBar.setValue(percentage);
        monthProgressBar.setString("월간 달성률: " + percentage + "%");
        
        // 2. 태그별 통계 계산 및 그래프 그리기
        Map<String, Integer> tagData = calculateTagStats(currentYearMonth);
        barChartPanel.setData(tagData);

        // 3. 메시지 및 상세 텍스트 업데이트
        updateMessageAndDetails(percentage, activeDays, daysInMonth, tagData);
    }
    
    // 태그 통계 계산 헬퍼
    private Map<String, Integer> calculateTagStats(YearMonth currentMonth) {
        Map<String, Integer> stats = new HashMap<>();
        List<Routine> routines = manager.getAllRoutines();
        
        for (Routine r : routines) {
            String tag = r.getTag();
            // 해당 루틴이 이번 달에 수행된 횟수 카운트
            long count = r.getLastCompletedDateMap().values().stream()
                    .filter(date -> YearMonth.from(date).equals(currentMonth))
                    .count();
            
            if (count > 0) {
                stats.put(tag, stats.getOrDefault(tag, 0) + (int)count);
            }
        }
        return stats;
    }

    // 메시지 업데이트 헬퍼
    private void updateMessageAndDetails(int percentage, long activeDays, int totalDays, Map<String, Integer> tagData) {
        String message;
        StringBuilder details = new StringBuilder();

        // 격려 메시지 로직 (기존 유지)
        if (percentage == 0) {
            message = "시작이 반입니다! 오늘부터 루틴을 시작해보세요.";
            details.append("아직 기록된 루틴 활동이 없습니다.\n작은 목표부터 하나씩 달성해보는 건 어떨까요?");
        } else if (percentage < 30) {
            message = "좋은 출발입니다! 꾸준함이 핵심이에요.";
            details.append("습관 형성 초기 단계입니다.\n연속 달성을 목표로 조금 더 힘내세요!");
        } else if (percentage < 70) {
            message = "아주 잘하고 있어요! 성실함이 빛을 발하네요.";
            details.append("안정적인 루틴 생활을 하고 계시네요.\n이 페이스를 유지하면 목표를 반드시 이룰 수 있습니다.");
        } else {
            message = "완벽합니다! 당신은 진정한 루틴 마스터!";
            details.append(String.format("놀라운 성과입니다! 총 %d일 중 %d일을 달성하셨습니다.\n당신의 끈기에 박수를 보냅니다!", totalDays, activeDays));
        }

        // 태그별 상세 정보 추가
        if (!tagData.isEmpty()) {
            details.append("\n\n[태그별 수행 횟수]\n");
            tagData.forEach((tag, count) -> details.append(" - ").append(tag).append(": ").append(count).append("회\n"));
        }

        messageLabel.setText(message); // ⭐ 화면에 표시
        statsDetailsArea.setText(details.toString());
    }
}