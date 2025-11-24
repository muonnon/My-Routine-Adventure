package J1103;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import J1103.Routine;
import J1103.RoutineModify;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JTable에서 마우스 우클릭 시 수정/삭제 팝업 메뉴를 띄우는 리스너입니다.
 */
public class PopupListener extends MouseAdapter {

    private final JTable table;
    private final RoutineManager manager;
    private final RoutineListView view; // 목록 뷰 갱신을 위해 참조

    public PopupListener(JTable table, RoutineManager manager, RoutineListView view) {
        this.table = table;
        this.manager = manager;
        this.view = view;
    }

    // 마우스 버튼이 떼어졌을 때 (플랫폼별 우클릭 인식 시점 대응)
    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    // 마우스 버튼이 눌렸을 때 (플랫폼별 우클릭 인식 시점 대응)
    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        // 팝업 트리거(보통 우클릭)인지 확인
        if (e.isPopupTrigger()) {
            
            // 클릭된 테이블의 행 인덱스 확인
            int r = table.rowAtPoint(e.getPoint());
            if (r >= 0 && r < table.getRowCount()) {
                // 해당 행을 선택 상태로 만듭니다.
                table.setRowSelectionInterval(r, r);
            } else {
                // 유효하지 않은 행이면 팝업 표시를 취소합니다.
                return;
            }

            // 루틴 ID와 이름을 가져옵니다. (Routine ID는 3번째 컬럼에 숨겨져 있음)
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            String routineId = (String) model.getValueAt(r, 3);
            String routineName = (String) model.getValueAt(r, 1);
            
            // 팝업 메뉴 생성
            JPopupMenu popup = new JPopupMenu();
            JMenuItem modifyItem = new JMenuItem("수정: " + routineName);
            JMenuItem deleteItem = new JMenuItem("삭제: " + routineName);

            // 1. 수정 메뉴 액션
            modifyItem.addActionListener(ae -> modifyRoutine(routineId));

            // 2. 삭제 메뉴 액션
            deleteItem.addActionListener(ae -> deleteRoutine(routineId, routineName));

            popup.add(modifyItem);
            popup.add(deleteItem);
            
            // 마우스 클릭 위치에 팝업을 표시
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    /**
     * 루틴 수정 다이얼로그를 띄웁니다.
     */
    private void modifyRoutine(String id) {
        // Manager를 통해 루틴 객체 전체를 가져옵니다.
        Routine routine = manager.getRoutine(id);

        if (routine != null) {
            // RoutineModify는 JDialog이며 RoutineListView(JFrame)를 부모로 가집니다.
            RoutineModify modifyDialog = new RoutineModify(view, manager, routine);
            modifyDialog.setVisible(true);

            // 수정 후 루틴 목록을 갱신합니다.
            view.loadAllRoutines();
        } else {
            JOptionPane.showMessageDialog(view, "루틴 정보를 찾을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 루틴을 삭제하고 확인 메시지를 표시합니다.
     */
    private void deleteRoutine(String id, String name) {
        int confirm = JOptionPane.showConfirmDialog(
                view, 
                "'" + name + "' 루틴을 정말로 삭제하시겠습니까?", 
                "루틴 삭제 확인", 
                JOptionPane.YES_NO_OPTION
            );
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (manager.deleteRoutine(id)) {
                JOptionPane.showMessageDialog(view, "'" + name + "' 루틴이 삭제되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                // 삭제 후 목록 갱신
                view.loadAllRoutines();
            } else {
                JOptionPane.showMessageDialog(view, "루틴 삭제에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}