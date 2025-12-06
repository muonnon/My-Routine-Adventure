package J1103;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // ⭐ 이 줄을 추가해야 합니다.
import java.awt.event.MouseAdapter; // -- 2025-11-05
import java.awt.event.MouseEvent;  // -- 2025-11-05
import java.time.DayOfWeek;
import java.time.LocalDate;

import javax.swing.event.TableModelEvent; // --2025-11-10
import javax.swing.event.TableModelListener; // TableModelListener도 같이 필요합니다

public class RoutineListView extends JFrame {

    private final RoutineManager manager;
    private final String[] DAYS = {"월", "화", "수", "목", "금", "토", "일"};
    private final String[] TABLE_HEADERS = {"완료", "루틴 이름", "태그", "Routine ID"}; // Routine ID는 사용자에게 보여주지 않는다.
    
    // 요일별 테이블 모델을 저장하여 데이터를 쉽게 업데이트합니다.
    private Map<String, DefaultTableModel> dayTableModels = new HashMap<>(); 

    public RoutineListView(RoutineManager manager) {
        this.manager = manager;
        setTitle("MRA --- 루틴 목록");
        setSize(600, 400);
        setLocationRelativeTo(null);
        
        // 메인 컨테이너에 요일별 탭 배치
        JTabbedPane tabbedPane = createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        
        loadAllRoutines(); // 초기 데이터 로드

        setVisible(true); 
    }

    private JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        for (String day : DAYS) {
            // ⭐ 2025-11-12: 모델 생성 시 TABLE_HEADERS를 사용하여 4개 컬럼을 만듭니다.
            DefaultTableModel model = new DefaultTableModel(TABLE_HEADERS, 0) {
                // 첫 번째 컬럼("완료")을 JCheckBox로 렌더링하도록 타입 지정
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Boolean.class : String.class;
                }
                
                // ⭐ 2025-11-12: 핵심 수정! 루틴이 이미 완료되었으면(true) 클릭 불가 처리 (체크박스 풀림 문제 해결)
                @Override
                public boolean isCellEditable(int row, int column) {
                    if (column == 3) return false; // ID는 수정 불가
                    if (column == 0) {
                        // 완료 여부를 가져옵니다.
                        Boolean isCompleted = (Boolean) getValueAt(row, 0); 
                        // 이미 완료되었으면(true) 수정(클릭) 불가
                        return !isCompleted; 
                    }
                    return true; // 이름, 태그는 수정 가능
                }
            };
            dayTableModels.put(day, model);

            JTable table = new JTable(model);
            table.getColumnModel().getColumn(3).setMinWidth(0);
            table.getColumnModel().getColumn(3).setMaxWidth(0);
            table.getColumnModel().getColumn(3).setWidth(0); // Routine ID 컬럼 숨김
            
			// --251119: 완료된 루틴 배경색 변경을 위한 셀 렌더러 설정
			table.setDefaultRenderer(Object.class, new RoutineCellRenderer(day, manager));		
			// --251119: 체크박스 클릭 이벤트 리스너 추가
			model.addTableModelListener(new RoutineCompletionListener(day));

			table.addMouseListener(new PopupListener(table, manager, this));

			tabbedPane.addTab(day + "요일", new JScrollPane(table));
		}
		return tabbedPane;
	}

	// --251119: 현재 요일을 기준으로 대상 요일이 미래인지 확인
	private boolean isFutureDay(String targetDay) {
		DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
		DayOfWeek targetDayOfWeek = getDayOfWeek(targetDay);

		if (targetDayOfWeek == null)
			return false;

		return targetDayOfWeek.getValue() > currentDay.getValue();
	}

	// --251119: 과거 요일인지 확인 (오늘 체크 가능)
	private boolean isTooPastDay(String targetDay) {
		DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
		DayOfWeek targetDayOfWeek = getDayOfWeek(targetDay);

		if (targetDayOfWeek == null)
			return false;

		// 현재 요일과 타겟 요일 사이의 일수 차이 계산
		int daysBetween = (currentDay.getValue() - targetDayOfWeek.getValue() + 7) % 7;

		// 2일 이상 지난 과거 요일이면 true 반환
		return daysBetween >= 1;
	}

	// --251119: 한글 요일 문자열을 DayOfWeek Enum으로 변환
	private DayOfWeek getDayOfWeek(String day) {
		switch (day) {
		case "월":
			return DayOfWeek.MONDAY;
		case "화":
			return DayOfWeek.TUESDAY;
		case "수":
			return DayOfWeek.WEDNESDAY;
		case "목":
			return DayOfWeek.THURSDAY;
		case "금":
			return DayOfWeek.FRIDAY;
		case "토":
			return DayOfWeek.SATURDAY;
		case "일":
			return DayOfWeek.SUNDAY;
		default:
			return null;
		}
	}

	// --251119: 루틴 목록 로딩 로직 (요일별 완료 상태 반영 + 자동 정렬)
	public void loadAllRoutines() {
		for (String day : DAYS) {
			List<Routine> dailyRoutines = manager.getRoutinesForDay(day);
			DefaultTableModel model = dayTableModels.get(day);

			model.setRowCount(0);

			for (Routine routine : dailyRoutines) {
				boolean isCompleted = routine.isCompletedForDay(day);
				model.addRow(new Object[] { isCompleted, routine.getName(), routine.getTag(), routine.getId() });
			}
		}
	}

	// --251119: 루틴 완료 상태 변경 이벤트를 처리하는 리스너 클래스
	private class RoutineCompletionListener implements TableModelListener {
		private final String day;

		public RoutineCompletionListener(String day) {
			this.day = day;
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			if (e.getType() == TableModelEvent.UPDATE) {
				int row = e.getFirstRow();
				int column = e.getColumn();

				if (column == 0) {
					DefaultTableModel model = (DefaultTableModel) e.getSource();
					Boolean isChecked = (Boolean) model.getValueAt(row, 0);
					String routineId = (String) model.getValueAt(row, 3);

					Routine routine = manager.getRoutine(routineId);
					if (routine == null)
						return;

					// 체크 해제 방지 로직
					if (!isChecked) {
						if (routine.isCompletedForDay(this.day)) {
							SwingUtilities.invokeLater(() -> model.setValueAt(true, row, 0));
							return;
						}
						return;
					}

					// 미래 요일 체크 방지
					if (isFutureDay(this.day)) {
						JOptionPane.showMessageDialog(RoutineListView.this, this.day + "은(는) 미래 요일이므로 루틴을 완료할 수 없습니다.",
								"경고", JOptionPane.WARNING_MESSAGE);
						SwingUtilities.invokeLater(() -> model.setValueAt(false, row, 0));
						return;
					}

					// 2일 이상 지난 과거 요일 체크 방지
					if (isTooPastDay(this.day)) {
						JOptionPane.showMessageDialog(RoutineListView.this,
								this.day + "요일은 하루 이상 지난 과거 요일이므로 루틴을 완료할 수 없습니다.\n(어제까지만 체크 가능합니다)", "경고",
								JOptionPane.WARNING_MESSAGE);
						SwingUtilities.invokeLater(() -> model.setValueAt(false, row, 0));
						return;
					}

					// 루틴 완료 처리
					boolean success = manager.completeRoutine(routineId, this.day);

					if (success) {
						loadAllRoutines();
					} else {
						System.out.println("루틴 완료 처리 실패 또는 이미 완료됨: " + routineId);
					}
				}
			}
		}
	}

	// --251119: 테이블 셀의 배경색을 커스터마이징하는 렌더러 클래스
	private class RoutineCellRenderer extends DefaultTableCellRenderer {
		private final String day;
		private final RoutineManager manager;

		public RoutineCellRenderer(String day, RoutineManager manager) {
			this.day = day;
			this.manager = manager;
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			String routineId = (String) table.getModel().getValueAt(row, 3);
			Routine routine = manager.getRoutine(routineId);

			// 완료된 루틴은 회색 배경 처리
			if (routine != null && routine.isCompletedForDay(day)) {
				c.setBackground(new Color(220, 220, 220));
			} else {
				if (isSelected) {
					c.setBackground(table.getSelectionBackground());
				} else {
					c.setBackground(table.getBackground());
				}
			}

			// 미래 요일 또는 2일 이상 지난 과거 요일의 루틴은 텍스트 회색 처리
			if (isFutureDay(day) || isTooPastDay(day)) {
				c.setForeground(Color.GRAY);
			} else {
				c.setForeground(table.getForeground());
			}

			return c;
		}
	}
}