package J1103;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * 태그별 통계 데이터를 받아 막대그래프로 그려주는 패널
 */
public class SimpleBarChartPanel extends JPanel {

    private Map<String, Integer> data; // 태그 이름, 횟수

    public SimpleBarChartPanel() {
        setPreferredSize(new Dimension(400, 200)); // 기본 크기
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("태그별 수행 현황"));
    }

    public void setData(Map<String, Integer> data) {
        this.data = data;
        repaint(); // 데이터가 바뀌면 다시 그리기
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data == null || data.isEmpty()) {
            g.drawString("데이터가 없습니다.", 20, 20);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        // 여백 설정
        int padding = 40;
        int graphWidth = panelWidth - (2 * padding);
        int graphHeight = panelHeight - (2 * padding);

        // 최대값 찾기 (그래프 높이 기준)
        int maxCount = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        if (maxCount == 0) maxCount = 1;

        int barWidth = graphWidth / data.size() / 2; // 막대 두께
        int x = padding + (barWidth / 2); // 시작 위치

        // 그래프 그리기
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String tag = entry.getKey();
            int count = entry.getValue();

            // 막대 높이 계산
            int barHeight = (int) ((double) count / maxCount * graphHeight);

            // 1. 막대 그리기
            g2.setColor(new Color(100, 149, 237)); // 옥수수꽃 파란색 (CornflowerBlue)
            g2.fillRect(x, panelHeight - padding - barHeight, barWidth, barHeight);

            // 2. 테두리 그리기
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(x, panelHeight - padding - barHeight, barWidth, barHeight);

            // 3. 횟수 표시 (막대 위)
            g2.setColor(Color.BLACK);
            g2.drawString(String.valueOf(count), x + (barWidth / 2) - 5, panelHeight - padding - barHeight - 5);

            // 4. 태그 이름 표시 (막대 아래)
            g2.drawString(tag, x, panelHeight - padding + 20);

            // 다음 막대 위치로 이동
            x += (graphWidth / data.size());
        }
        
        // X축 선 그리기
        g2.drawLine(padding, panelHeight - padding, panelWidth - padding, panelHeight - padding);
    }
}