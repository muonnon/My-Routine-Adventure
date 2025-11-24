// RoutineModificationDialog.java (새 파일 또는 RoutineListView 내부 클래스로 정의)
package J1103;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors; // ⭐ stream 사용을 위해 추가


//수정은 복잡한 입력이 필요 --> 별도 class 생성
//삭제는 단순한 확인만 필요 --> RoutineListView 내부 클래스에서 처리 

public class RoutineModify extends JDialog {

    private JTextField nameField;
    private JComboBox<String> tagComboBox;
    private JCheckBox[] dayCheckBoxes;
    private final String[] DAYS = {"월", "화", "수", "목", "금", "토", "일"};
    
    // 수정된 데이터를 저장할 변수
    private boolean confirmed = false;
    private RoutineManager manager;
    private Routine originalRoutine;

    public RoutineModify(JFrame parent, RoutineManager manager, Routine routine) {
        super(parent, "루틴 수정: " + routine.getName(), true); // 모달 다이얼로그
        this.manager = manager;
        this.originalRoutine = routine;

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // 1. 입력 필드가 포함된 패널 생성 및 기존 데이터 로드
        JPanel inputPanel = createInputPanel();
        loadOriginalData(routine);
        add(inputPanel, BorderLayout.CENTER);

        // 2. 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("저장");
        JButton cancelButton = new JButton("취소");
        
        saveButton.addActionListener(e -> {
            if(saveChanges()) {
                confirmed = true;
                dispose(); // 다이얼로그 닫기
            }
        });
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose(); // 다이얼로그 닫기
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 창이 닫힐 때 confirmed 상태를 false로 유지
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                confirmed = false;
            }
        });
    }
    
    // 수정 성공 여부를 외부에 알리는 Getter
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * 입력 필드를 생성하고 배치합니다.
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. 루틴 이름
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("루틴 이름:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        // 2. 태그
        String[] tags = {"공부", "운동", "생활", "취미", "기타"};
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(new JLabel("태그:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        tagComboBox = new JComboBox<>(tags);
        panel.add(tagComboBox, gbc);

        // 3. 반복 요일
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; panel.add(new JLabel("반복 요일:"), gbc);
        
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dayCheckBoxes = new JCheckBox[DAYS.length];
        for (int i = 0; i < DAYS.length; i++) {
            dayCheckBoxes[i] = new JCheckBox(DAYS[i]);
            dayPanel.add(dayCheckBoxes[i]);
        }
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        panel.add(dayPanel, gbc);
        
        return panel;
    }

    /**
     * 수정하려는 루틴의 기존 데이터를 UI 필드에 로드합니다.
     */
    private void loadOriginalData(Routine routine) {
        nameField.setText(routine.getName());
        tagComboBox.setSelectedItem(routine.getTag());

        // 반복 요일 체크박스 설정
        List<String> currentDays = routine.getRepeatDays();
        for (int i = 0; i < DAYS.length; i++) {
            if (currentDays.contains(DAYS[i])) {
                dayCheckBoxes[i].setSelected(true);
            }
        }
    }

    /**
     * Manager에 수정된 데이터를 저장합니다.
     */
    private boolean saveChanges() {
        String newName = nameField.getText().trim();
        String newTag = (String) tagComboBox.getSelectedItem();
        List<String> newDays = new ArrayList<>();
        
        for (int i = 0; i < DAYS.length; i++) {
            if (dayCheckBoxes[i].isSelected()) {
                newDays.add(DAYS[i]);
            }
        }

        if (newName.isEmpty() || newDays.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름과 반복 요일을 모두 선택해야 합니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // RoutineManager에 업데이트 요청
        boolean success = manager.updateRoutine(
            originalRoutine.getId(), 
            newName, 
            newTag, 
            newDays
        );
        
        if (success) {
            JOptionPane.showMessageDialog(this, "루틴이 성공적으로 수정되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "루틴 수정에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}