// 월간 통계 화면을 담당하는 패널 클래스

package J1103;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

/**
 * // 12/01: [신규] 월간 통계 및 격려 메시지를 표시하는 패널 클래스 (REQ-029, REQ-030 구현)
 */
public class StatisticsPanel extends JPanel {

    private final Player player;
    private JLabel monthTitleLabel;
    private JLabel countLabel;
    private JProgressBar monthProgressBar;
    private JLabel messageLabel; // 격려 메시지 표시용
    private JTextArea statsDetailsArea;

    public StatisticsPanel(Player player) {
        this.player = player;
        setLayout(new BorderLayout(20, 20)); // 여백 설정
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 테두리 여백

        initUI();
    }

    private void initUI() {
        // 1. 상단: 월 제목
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        monthTitleLabel = new JLabel();
        monthTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        topPanel.add(monthTitleLabel);
        
        add(topPanel, BorderLayout.NORTH);

        // 2. 중앙: 통계 그래프 및 수치
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 10)); // 4행 1열

        // 달성 일수 표시
        countLabel = new JLabel("", JLabel.CENTER);
        countLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 18));
        
        // 진행률 바 (달성률)
        monthProgressBar = new JProgressBar(0, 100);
        monthProgressBar.setStringPainted(true);
        monthProgressBar.setForeground(new Color(50, 205, 50)); // 라임 그린
        monthProgressBar.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        
        // 격려 메시지 (REQ-030)
        messageLabel = new JLabel("", JLabel.CENTER);
        messageLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        messageLabel.setForeground(Color.BLUE);

        centerPanel.add(countLabel);
        centerPanel.add(monthProgressBar);
        centerPanel.add(new JLabel("▼ 오늘의 한마디 ▼", JLabel.CENTER)); // 구분선 느낌
        centerPanel.add(messageLabel);

        add(centerPanel, BorderLayout.CENTER);

        // 3. 하단: 상세 텍스트 (추가 정보)
        statsDetailsArea = new JTextArea(5, 20);
        statsDetailsArea.setEditable(false);
        statsDetailsArea.setBackground(new Color(245, 245, 245));
        statsDetailsArea.setBorder(BorderFactory.createTitledBorder("상세 분석"));
        
        add(new JScrollPane(statsDetailsArea), BorderLayout.SOUTH);

        // 초기 데이터 로드
        updateStatistics();
    }

    /**
     * // 12/01: 현재 날짜 기준으로 통계를 갱신하는 메서드
     * 탭이 선택될 때마다 MainDashboard에서 호출됨
     */
    public void updateStatistics() {
        YearMonth currentYearMonth = YearMonth.now();
        int daysInMonth = currentYearMonth.lengthOfMonth();
        
        // 플레이어의 수행 기록(streak) 중 이번 달에 해당하는 날짜만 카운트
        Set<LocalDate> streaks = player.getStreakDates();
        long activeDays = streaks.stream()
                .filter(date -> YearMonth.from(date).equals(currentYearMonth))
                .count();

        // 1. UI 텍스트 갱신
        monthTitleLabel.setText(currentYearMonth.getYear() + "년 " + currentYearMonth.getMonthValue() + "월 리포트");
        countLabel.setText(String.format("이번 달 총 %d일 루틴을 수행했습니다!", activeDays));
        
        // 2. 진행률(%) 계산
        int percentage = (int) ((activeDays / (double) daysInMonth) * 100);
        monthProgressBar.setValue(percentage);
        monthProgressBar.setString("월간 달성률: " + percentage + "%");

        // 3. 격려 메시지 및 상세 분석 업데이트 (REQ-030)
        updateMessageAndDetails(percentage, activeDays, daysInMonth);
    }

    private void updateMessageAndDetails(int percentage, long activeDays, int totalDays) {
        String message;
        String details;

        if (percentage == 0) {
            message = "시작이 반입니다! 오늘부터 루틴을 시작해보세요.";
            details = "아직 기록된 루틴 활동이 없습니다.\n작은 목표부터 하나씩 달성해보는 건 어떨까요?";
        } else if (percentage < 30) {
            message = "좋은 출발입니다! 꾸준함이 핵심이에요.";
            details = "습관 형성 초기 단계입니다.\n연속 달성을 목표로 조금 더 힘내세요!";
        } else if (percentage < 70) {
            message = "아주 잘하고 있어요! 성실함이 빛을 발하네요.";
            details = "안정적인 루틴 생활을 하고 계시네요.\n이 페이스를 유지하면 목표를 반드시 이룰 수 있습니다.";
        } else {
            message = "완벽합니다! 당신은 진정한 루틴 마스터!";
            details = String.format("놀라운 성과입니다! 총 %d일 중 %d일을 달성하셨습니다.\n당신의 끈기에 박수를 보냅니다!", totalDays, activeDays);
        }

        messageLabel.setText(message);
        statsDetailsArea.setText(details);
    }
}