package J1103;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class RoutineListView extends JFrame {

    private final RoutineManager manager;
    private final String[] DAYS = {"월", "화", "수", "목", "금", "토", "일"};
    private final String[] TABLE_HEADERS = {"완료", "루틴 이름", "태그", "Routine ID"};
    
    // 요일별 테이블 모델을 저장하여 데이터를 쉽게 업데이트합니다.
    private Map<String, DefaultTableModel> dayTableModels = new HashMap<>();
    // ⭐ 요일별 테이블 참조 저장 (과거/미래 요일 클릭 제어용)
    private Map<String, JTable> dayTables = new HashMap<>(); 

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
        String todayDay = DateUtil.getTodayKoreanDay();
        
        for (String day : DAYS) {
            final boolean isToday = day.equals(todayDay); // ⭐ 오늘인지 확인
            
            // 테이블 모델 생성
            DefaultTableModel model = new DefaultTableModel(TABLE_HEADERS, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) return Boolean.class;
                    return String.class; 
                }
                @Override
                public boolean isCellEditable(int row, int column) {
                    // ⭐ 오늘 요일만 체크박스 편집 가능, 과거/미래 요일은 클릭 불가
                    if (!isToday) return false;
                    
                    // 완료된 루틴은 편집 불가
                    if (column == 0) {
                        Boolean isCompleted = (Boolean) getValueAt(row, 0);
                        return isCompleted == null || !isCompleted;
                    }
                    return false;
                }
            };
            dayTableModels.put(day, model);
            
            JTable table = new JTable(model);
            dayTables.put(day, table); // ⭐ 테이블 참조 저장
            
            // 1. Routine ID 컬럼 숨기기 (사용자에게는 보이지 않게)
            table.getColumnModel().getColumn(3).setMinWidth(0);
            table.getColumnModel().getColumn(3).setMaxWidth(0);
            table.getColumnModel().getColumn(3).setWidth(0);

            // 2. 컬럼 너비 조정
            table.getColumnModel().getColumn(0).setPreferredWidth(50);  // 완료
            table.getColumnModel().getColumn(1).setPreferredWidth(200); // 루틴 이름
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // 태그
            
            // 3. 렌더러 설정 (⭐ 오늘 요일 여부 전달)
            RoutineRenderer renderer = new RoutineRenderer(); 
            renderer.setIsToday(isToday); // ⭐ 오늘 요일인지 설정
            table.setDefaultRenderer(Boolean.class, renderer);
            table.setDefaultRenderer(String.class, renderer);
            
            // 4. ⭐ 과거/미래 요일 클릭 시 팝업 표시
            if (!isToday) {
                table.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int column = table.columnAtPoint(e.getPoint());
                        // 체크박스 컬럼(0번)을 클릭했을 때만 팝업 표시
                        if (column == 0) {
                            JOptionPane.showMessageDialog(
                                RoutineListView.this,
                                "과거/미래 루틴은 체크할 수 없습니다.",
                                "알림",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    }
                });
            }
            
            // 5. 우클릭 팝업 리스너 추가
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
     * ⭐ 완료된 루틴은 아래로 정렬됩니다.
     */
    public void loadAllRoutines() {
        // 현재 요일을 구하는 헬퍼 메서드 (DayOfWeek를 한글 요일로 변환)
        String todayDay = DateUtil.getTodayKoreanDay();
        
        for (String day : DAYS) {
            DefaultTableModel model = dayTableModels.get(day);
            // 1. 기존 데이터 모두 삭제
            model.setRowCount(0); 

            // 2. RoutineManager로부터 해당 요일의 루틴 목록을 가져옵니다.
            List<Routine> routines = manager.getRoutinesByDay(day);

            // ⭐ 3. 완료되지 않은 루틴을 먼저, 완료된 루틴을 나중에 정렬
            List<Routine> sortedRoutines = new ArrayList<>(routines);
            sortedRoutines.sort((r1, r2) -> {
                boolean c1 = r1.isCompletedForDay(day);
                boolean c2 = r2.isCompletedForDay(day);
                // 미완료(false)가 먼저, 완료(true)가 나중에
                return Boolean.compare(c1, c2);
            });

            // 4. 테이블에 데이터 추가 (정렬된 순서로)
            for (Routine routine : sortedRoutines) {
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
        
        // ⭐ 오늘의 요일 탭으로 강제 이동
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
        int todayIndex = java.util.Arrays.asList(DAYS).indexOf(todayDay);
        if (todayIndex >= 0) {
            tabbedPane.setSelectedIndex(todayIndex);
        }
    }
}