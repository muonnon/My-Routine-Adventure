//플레이어의 인벤토리 정보를 표시할 창
package J1103;

import javax.swing.*;
import java.awt.*;

public class InventoryView extends JFrame {
    
    private final Player player; // Player 객체 참조

    public InventoryView(Player player) {
        this.player = player;
        
        setTitle("플레이어 인벤토리 - " + player.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 이 창만 닫기
        setSize(500, 400);
        setLocationRelativeTo(null); // 화면 중앙에 표시
        
        initUI();
        setVisible(true); // 생성과 동시에 보이게 설정
    }
    
    private void initUI() {
        // 메인 패널 설정
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); // 여백 추가
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상단 제목 및 상태 표시
        JLabel titleLabel = new JLabel("인벤토리", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 중앙 아이템 목록 패널 (임시 데이터)
        JTextArea inventoryArea = new JTextArea();
        inventoryArea.setEditable(false);
        inventoryArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        
        // 현재 플레이어 정보 표시
        String inventoryContent = String.format(
            "===============================\n" +
            "플레이어: %s (Lv. %d)\n" +
            "보유 골드: %d G\n" +
            "===============================\n\n" +
            "--- 아이템 목록 (향후 Grid/Table로 구현 예정) ---\n" +
            "1. 아이템1 (3개)\n" +
            "2. 아이템2 (1개)\n" +
            "3. 아이템3 (1개)\n" +
            "4. ...\n",
            player.getName(), player.getLevel(), player.getGold()
        );
        inventoryArea.setText(inventoryContent);
        
        JScrollPane scrollPane = new JScrollPane(inventoryArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 하단 닫기 버튼
        JButton closeButton = new JButton("인벤토리 닫기");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
}