package J1103;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RoutineManagerGUI extends JFrame {

	// ⭐ 필드 수정: Manager를 생성하지 않고 외부에서 받도록 변경 (11/11)
	private final RoutineManager manager; 
	
    // ⭐ [오류 수정]: 불필요한 내부 Routine 클래스 정의 전체 삭제 (11/11)
    /*
	public class Routine {
	    private String name;
	    private String tag; // 태그 (예: 공부, 운동, 생활)
	    private List<String> repeatDays; // 반복 요일 (예: "월", "수", "금")

	    public Routine(String name, String tag, List<String> repeatDays) {
	        this.name = name;
	        this.tag = tag;
	        this.repeatDays = repeatDays;
	    }
        // Getter 메서드 (필요한 경우 Setter도 추가 가능)
	    public String getName() {
	        return name;
	    }

	    public String getTag() {
	        return tag;
	    }

	    public List<String> getRepeatDays() {
	        return repeatDays;
	    }

	    @Override
	    public String toString() {
	        return name + " [" + tag + "] (" + String.join(", ", repeatDays) + ")";
	    }
	}
    */
	
	// GUI 컴포넌트
    private JTextField routineNameField;
    private JComboBox<String> tagComboBox;
    private JCheckBox[] dayCheckBoxes;
    
    private final String[] DAYS = {"월", "화", "수", "목", "금", "토", "일"};
    private final String[] TAGS = {"공부", "운동", "생활", "취미", "기타"};
    

	// ⭐ 생성자 수정: Manager 객체를 인자로 받음 (11/11)
	public RoutineManagerGUI(RoutineManager manager) {
        this.manager = manager; 
		setTitle("MRA --- 루틴 생성/관리");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 메인 창에 영향 없이 닫기
		setSize(500, 300);
		setLocationRelativeTo(null);
		
		add(createInputPanel());
		
		setVisible(true);
	}
	
	/**
	 * 루틴 입력/생성 UI 패널을 만듭니다.
	 */
	private JPanel createInputPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5); // 여백
		gbc.fill = GridBagConstraints.HORIZONTAL; // 가로로 채우기

		// --- 1. 루틴 이름 입력 ---
		gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; // 라벨
		panel.add(new JLabel("루틴 이름:"), gbc);
		
		gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; // 텍스트 필드
		routineNameField = new JTextField(20);
		panel.add(routineNameField, gbc);

		// --- 2. 태그 선택 ---
		gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; 
		panel.add(new JLabel("태그:"), gbc);
		
		gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
		tagComboBox = new JComboBox<>(TAGS);
		panel.add(tagComboBox, gbc);

        // --- 3. 반복 요일 설정 ---
		gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; 
		panel.add(new JLabel("반복 요일:"), gbc);
        
        // 요일 체크박스를 담을 패널
        JPanel dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dayCheckBoxes = new JCheckBox[DAYS.length];
        for (int i = 0; i < DAYS.length; i++) {
            dayCheckBoxes[i] = new JCheckBox(DAYS[i]);
            dayPanel.add(dayCheckBoxes[i]);
        }
		gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
		panel.add(dayPanel, gbc); // 요일 패널 추가
        
		// --- 4. 루틴 생성 및 목록 보기 버튼 ---
		gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE;
        
        
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // 버튼을 담을 FlowLayout 
        // 루틴 생성 버튼 (기존 기능 유지)
        JButton addButton = new JButton("루틴 생성");
        addButton.addActionListener(e -> createRoutine());
        buttonPanel.add(addButton);
        
        // ⭐ 새로 추가된 '목록 보기' 버튼
        JButton listButton = new JButton("루틴 목록 보기");
        listButton.addActionListener(e -> {
            // RoutineManager 객체를 전달하여 새 목록 창 생성
            new RoutineListView(manager); 
        });
        
        buttonPanel.add(listButton);
        
        panel.add(buttonPanel, gbc); // buttonPanel을 inputPanel에 추가
		
		return panel;
	}
	
	/**
	 * 루틴 생성 버튼 클릭 시 동작하는 메서드
	 */
    // ⭐ 루틴 생성 로직 수정 (11/11)
    private void createRoutine() {
        String routineName = routineNameField.getText().trim();
        String selectedTag = (String) tagComboBox.getSelectedItem();
        
        List<String> selectedDays = new ArrayList<>();
        for (int i = 0; i < DAYS.length; i++) {
            if (dayCheckBoxes[i].isSelected()) {
                selectedDays.add(DAYS[i]);
            }
        }
        
        // 유효성 검사 (이전과 동일)
        if (routineName.isEmpty() || selectedDays.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름과 반복 요일을 모두 선택해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ⭐ 핵심: RoutineManager에 루틴 생성 요청 (11/11)
        manager.addRoutine(routineName, selectedTag, selectedDays); 
        
        // 3. 확인 메시지 및 입력 필드 초기화 (기존 코드 유지)
        JOptionPane.showMessageDialog(this, routineName + " 루틴이 생성되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
        routineNameField.setText("");
        for (JCheckBox cb : dayCheckBoxes) {
            cb.setSelected(false);
        }
    }
}