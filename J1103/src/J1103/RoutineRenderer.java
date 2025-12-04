package J1103;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 루틴 목록 테이블의 셀을 렌더링하는 커스텀 렌더러입니다.
 * - Column 0 (완료): 체크박스 렌더링 및 완료 시 비활성화
 * - 완료된 루틴: 배경 회색, 글자 검정
 * - 과거/미래 루틴: 글자 회색
 */
public class RoutineRenderer extends JCheckBox implements TableCellRenderer {

    // 다른 컬럼(이름, 태그)의 기본 렌더링을 위해 DefaultTableCellRenderer 객체를 사용합니다.
    private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
    
    // ⭐ 오늘 요일인지 여부 (외부에서 설정)
    private boolean isToday = true;

    public RoutineRenderer() {
        setOpaque(true); // 배경색 설정을 위해 필수
        setHorizontalAlignment(SwingConstants.CENTER); // 체크박스 중앙 정렬
    }
    
    /**
     * 현재 탭이 오늘 요일인지 설정합니다.
     * @param isToday 오늘 요일이면 true
     */
    public void setIsToday(boolean isToday) {
        this.isToday = isToday;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus, 
                                                 int row, int column) {
        
        Component c;
        // ⭐ 형 변환 전에 null 체크 추가 (데이터 로딩 오류 방지)
        Object completedValue = table.getModel().getValueAt(row, 0);
        Boolean isCompleted = (completedValue != null) ? (Boolean) completedValue : false;

        // 1. 체크박스 컬럼(Column 0) 처리
        if (column == 0 && value instanceof Boolean) {
            this.setSelected((Boolean) value);
            // 완료된 루틴 또는 과거/미래 요일은 체크박스 비활성화
            this.setEnabled(!isCompleted && isToday); 
            this.setBorderPainted(false);
            c = this;
            
        } else {
            // 2. 다른 컬럼(이름, 태그, ID) 처리
            c = defaultRenderer.getTableCellRendererComponent(table, value, 
                                                               isSelected, hasFocus, 
                                                               row, column);
        }
        
        // 3. 색상 변경 로직
        Color bgColor;
        Color fgColor;
        
        if (isCompleted) {
            // ⭐ 완료된 루틴: 배경 회색, 글자 검정
            bgColor = isSelected ? new Color(210, 210, 210) : new Color(240, 240, 240);
            fgColor = Color.BLACK;
        } else if (!isToday) {
            // ⭐ 과거/미래 루틴: 배경 기본, 글자 회색
            bgColor = isSelected ? table.getSelectionBackground() : table.getBackground();
            fgColor = Color.GRAY;
        } else {
            // 오늘의 미완료 루틴: 기본 색상
            if (isSelected) {
                bgColor = table.getSelectionBackground();
                fgColor = table.getSelectionForeground();
            } else {
                bgColor = table.getBackground();
                fgColor = table.getForeground();
            }
        }
        
        c.setBackground(bgColor);
        c.setForeground(fgColor);
        
        if (column == 0) {
            this.setBackground(bgColor);
            this.setForeground(fgColor);
        }
        
        // 4. 셀 내용 정렬
        if (c instanceof JLabel) {
             // 루틴 이름과 태그는 왼쪽 정렬 (1, 2 컬럼)
             if (column == 1 || column == 2) {
                 ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
             } else {
                 // 나머지 컬럼(완료, ID)은 중앙 정렬
                 ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
             }
        }
        
        return c;
    }
}