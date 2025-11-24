package J1103;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 루틴 목록 테이블의 셀을 렌더링하는 커스텀 렌더러입니다.
 * - Column 0 (완료): 체크박스 렌더링 및 완료 시 비활성화
 * - 모든 셀: 완료된 루틴일 경우 배경/글자 색상을 회색으로 변경
 */
public class RoutineRenderer extends JCheckBox implements TableCellRenderer { // ⭐ 최상위 클래스로 변경

    // 다른 컬럼(이름, 태그)의 기본 렌더링을 위해 DefaultTableCellRenderer 객체를 사용합니다.
    private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

    public RoutineRenderer() {
        setOpaque(true); // 배경색 설정을 위해 필수
        setHorizontalAlignment(SwingConstants.CENTER); // 체크박스 중앙 정렬
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
            this.setEnabled(!isCompleted); // 완료된 루틴은 체크박스 비활성화
            this.setBorderPainted(false); // 체크박스 자체의 경계선을 그리지 않음
            c = this; // JCheckBox (this) 반환
            
        } else {
            // 2. 다른 컬럼(이름, 태그, ID) 처리
            c = defaultRenderer.getTableCellRendererComponent(table, value, 
                                                               isSelected, hasFocus, 
                                                               row, column);
        }
        
        // 3. 색상 변경 로직 (완료 루틴 회색)
        if (isCompleted) {
            // 완료된 루틴은 연한 회색 배경과 어두운 회색 전경색으로 설정
            Color grayBackground = new Color(240, 240, 240); 
            Color darkGrayForeground = Color.GRAY; 
            
            c.setBackground(isSelected ? new Color(210, 210, 210) : grayBackground);
            c.setForeground(darkGrayForeground);
            
            // JCheckBox 렌더링 시 배경색도 일치시킵니다.
            if (column == 0) {
                 this.setBackground(isSelected ? new Color(210, 210, 210) : grayBackground);
                 this.setForeground(darkGrayForeground);
            }

        } else {
            // 미완료 루틴은 기본 색상
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
                if (column == 0) {
                    this.setBackground(table.getSelectionBackground());
                    this.setForeground(table.getSelectionForeground());
                }
            } else {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
                if (column == 0) {
                    this.setBackground(table.getBackground());
                    this.setForeground(table.getForeground());
                }
            }
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