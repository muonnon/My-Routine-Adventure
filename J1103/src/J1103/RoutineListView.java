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
        
        // 탭 패널 초기화
        JTabbedPane tabbedPane = initTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        
        // 테이블 모델 리스너 초기화 (체크박스 클릭 처리)
        initTableListeners();
    }
    
    /**
     * 요일별 탭 패널을 생성하고 각 탭에 테이블을 추가합니다.
     */
    private JTabbedPane initTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        for (String day : DAYS) {
            // 테이블 모델 생성
            DefaultTableModel model = new DefaultTableModel(TABLE_HEADERS, 0) {
                // 체크박스 컬럼(0번)을 Boolean 타입으로 설정하여 체크박스로 렌더링되게 합니다.
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) return Boolean.class;
                    // Routine ID 컬럼(3번)은 숨기므로 String 타입으로 둡니다.
                    return String.class; 
                }
                // Routine ID 컬럼(3번)을 제외한 나머지 컬럼은 편집 가능하도록 설정
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0; // 완료(체크박스) 컬럼만 편집 가능
                }
            };
            dayTableModels.put(day, model);
            
            JTable table = new JTable(model);
            
            // 1. Routine ID 컬럼 숨기기 (사용자에게는 보이지 않게)
            table.getColumnModel().getColumn(3).setMinWidth(0);
            table.getColumnModel().getColumn(3).setMaxWidth(0);
            table.getColumnModel().getColumn(3).setWidth(0);

            // 2. 컬럼 너비 조정
            table.getColumnModel().getColumn(0).setPreferredWidth(50);  // 완료
            table.getColumnModel().getColumn(1).setPreferredWidth(200); // 루틴 이름
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // 태그
            
            // 3. 렌더러 설정
            // Custom Renderer는 RoutineManager가 필요함.
            RoutineRenderer renderer = new RoutineRenderer(); 
            table.setDefaultRenderer(Boolean.class, renderer);
            table.setDefaultRenderer(String.class, renderer);
            
            // 4. 우클릭 팝업 리스너 추가
            table.addMouseListener(new PopupListener(table, manager, this));

            tabbedPane.addTab(day, new JScrollPane(table));
        }
        
        return tabbedPane;
    }
    
    /**
     * 테이블 모델에 리스너를 추가하여 '완료' 체크박스 클릭 시 루틴을 완료 처리합니다.
     */
    private void initTableListeners() {
        for (Map.Entry<String, DefaultTableModel> entry : dayTableModels.entrySet()) {
            String day = entry.getKey();
            DefaultTableModel model = entry.getValue();
            
            // TableModelListener는 데이터가 변경될 때마다 호출됩니다.
            model.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    // 0번 컬럼(체크박스)이 변경되었을 때만 처리
                    if (e.getColumn() == 0 && e.getType() == TableModelEvent.UPDATE) {
                        int row = e.getFirstRow();
                        
                        // 현재 상태 (클릭 후의 상태)
                        Boolean isChecked = (Boolean) model.getValueAt(row, 0); 

                        // 체크되었을 때만 완료 처리 로직 실행
                        if (isChecked) {
                            String routineId = (String) model.getValueAt(row, 3);
                            
                            // 루틴 완료 처리
                            boolean success = manager.completeRoutine(routineId, day);

                            if (success) {
                                // 완료 처리 성공 시 테이블 UI 갱신 (색상 변경 등을 위해)
                                loadAllRoutines();
                            } else {
                                // 완료 처리 실패(이미 완료된 경우 등) 시 체크박스를 다시 해제해야 합니다.
                                // 재귀 호출 방지를 위해 리스너를 잠시 제거 후 값 변경
                                model.removeTableModelListener(this);
                                model.setValueAt(false, row, 0); 
                                model.addTableModelListener(this);
                                System.out.println("루틴 완료 처리 실패 또는 이미 완료됨: " + routineId);
                            }
                        }
                    }
                }
            });
        }
    }
    
    /**
     * 모든 요일의 테이블 데이터를 갱신합니다.
     * RoutineModify, RoutineManagerGUI, Checkbox 클릭 등 여러 곳에서 호출됩니다.
     */
    public void loadAllRoutines() {
        // 현재 요일을 구하는 헬퍼 메서드 (DayOfWeek를 한글 요일로 변환)
        String todayDay = getKoreanDayOfWeek(LocalDate.now().getDayOfWeek());
        
        for (String day : DAYS) {
            DefaultTableModel model = dayTableModels.get(day);
            // 1. 기존 데이터 모두 삭제
            model.setRowCount(0); 

            // 2. RoutineManager로부터 해당 요일의 루틴 목록을 가져옵니다.
            // ⭐ 오류 수정: 메서드 이름을 getRoutinesByDay로 변경
            List<Routine> routines = manager.getRoutinesByDay(day); // 수정(11/21) 메서드 이름 통일: getRoutinesForDay -> getRoutinesByDay

            // 3. 테이블에 데이터 추가
            for (Routine routine : routines) {
                // 해당 요일에 오늘 완료했는지 확인
                boolean isCompleted = routine.isCompletedForDay(day); 
                
                // 테이블에 표시할 데이터 배열 생성
                // { 완료(Boolean), 이름(String), 태그(String), ID(String) }
                Object[] rowData = {
                    isCompleted, 
                    routine.getName(), 
                    routine.getTag(), 
                    routine.getId()
                };
                model.addRow(rowData);
            }
        }
        
        // ⭐ 오늘의 요일 탭으로 강제 이동 (선택적)
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
        int todayIndex = java.util.Arrays.asList(DAYS).indexOf(todayDay);
        if (todayIndex >= 0) {
            tabbedPane.setSelectedIndex(todayIndex);
        }
    }
    
    /**
     * java.time.DayOfWeek를 한글 요일 문자열로 변환합니다.
     */
    private String getKoreanDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.KOREAN);
    }
}