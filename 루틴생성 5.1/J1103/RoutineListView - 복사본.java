package J1103;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // ⭐ 이 줄을 추가해야 합니다.
import java.awt.event.MouseAdapter; // -- 2025-11-05
import java.awt.event.MouseEvent;  // -- 2025-11-05
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

            // ⭐ 2025-11-12: 체크박스 클릭 이벤트 리스너 추가 (루틴 완료 처리)
            model.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    // 완료 컬럼(0)의 데이터가 변경되었을 때만 처리합니다.
                    if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
                        int row = e.getFirstRow();
                        DefaultTableModel sourceModel = (DefaultTableModel)e.getSource();
                        
                        // 현재 체크 상태
                        boolean isChecked = (Boolean) sourceModel.getValueAt(row, 0); 
                        String routineId = (String) sourceModel.getValueAt(row, 3); // ID 가져오기

                        // 루틴이 체크(완료)되었을 때만 로직을 실행합니다.
                        if (isChecked) {
                            // 1. RoutineManager에 완료 처리 요청 (보상 지급 및 날짜 기록)
                            boolean success = manager.completeRoutine(routineId);

                            if (!success) {
                                // 2. 완료 실패 시 (이미 완료된 경우 등), 체크박스 상태를 되돌립니다. 
                                // (isCellEditable로 대부분 방지되나, 안전을 위해 추가)
                                sourceModel.setValueAt(false, row, 0);
                            }
                            // 성공 시 isCellEditable에 의해 자동으로 클릭이 비활성화됩니다.
                        }
                    }
                }
            });

            // ⭐ 테이블에 우클릭 팝업 메뉴 리스너를 추가합니다. 2025-11-05
            table.addMouseListener(new PopupListener(table, manager, this));
            
            tabbedPane.addTab(day + "요일", new JScrollPane(table));
        }
        return tabbedPane;
    }

    /**
     * RoutineManager에서 데이터를 가져와 모든 테이블 모델을 갱신합니다.
     */
    public void loadAllRoutines() {
        // 1. 모든 테이블 모델 초기화
        for (DefaultTableModel model : dayTableModels.values()) {
            model.setRowCount(0);
        }

        // 2. Manager에서 모든 루틴을 가져옵니다.
        List<Routine> allRoutines = manager.getAllRoutines(); 
        // ⭐ allRoutines의 타입은 List<Routine>이어야 합니다.

        // 3. 루틴을 반복 요일에 따라 해당하는 테이블에 추가합니다.
        for (Routine routine : allRoutines) {
            Object[] rowData = new Object[]{
                routine.isCompletedToday(), // ⭐ 오늘 완료 여부로 초기 체크 상태 설정 (11/11)
                routine.getName(),
                routine.getTag(),
                routine.getId() // ⭐ 숨겨진 마지막 컬럼에 ID를 저장합니다.
            };

            for (String day : routine.getRepeatDays()) {
                DefaultTableModel model = dayTableModels.get(day);
                if (model != null) {
                    model.addRow(rowData);
                }
            }
        }
    }
    
    // ... (PopupListener 클래스 및 나머지 코드는 기존과 동일하게 유지)
    // 이 클래스 내부에 PopupListener가 정의되어 있을 것으로 가정합니다.
    // ...
}