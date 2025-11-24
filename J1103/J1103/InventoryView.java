package J1103;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class InventoryView extends JFrame {
    
    private final Player player;
    private JTable equipTable;    // 장착 장비 테이블
    private JTable inventoryTable; // 가방 아이템 테이블
    private DefaultTableModel equipModel;
    private DefaultTableModel inventoryModel;

    public InventoryView(Player player) {
        this.player = player;
        
        setTitle("플레이어 인벤토리 - " + player.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        initUI();
        refreshData(); // 데이터 로드
        
        setVisible(true);
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 1. 상단: 플레이어 요약 정보
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        JLabel nameLabel = new JLabel(player.getName() + " (Lv." + player.getLevel() + ") | 골드: " + player.getGold() + " G", JLabel.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        
        // 보너스 스탯 표시
        String bonusText = String.format("적용 효과: 공격력 +%d / 추가경험치 +%d", 
                player.getTotalBonusDamage(), player.getTotalBonusExp());
        JLabel statLabel = new JLabel(bonusText, JLabel.CENTER);
        statLabel.setForeground(Color.BLUE);
        
        statusPanel.add(nameLabel);
        statusPanel.add(statLabel);
        mainPanel.add(statusPanel, BorderLayout.NORTH);
        
        // 2. 중앙: 탭 패널 (착용 장비 / 가방)
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 탭 1: 착용 중인 장비 (Equipped)
        JPanel equipPanel = new JPanel(new BorderLayout());
        String[] equipCols = {"부위", "아이템 이름", "효과"};
        equipModel = new DefaultTableModel(equipCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        equipTable = new JTable(equipModel);
        equipPanel.add(new JScrollPane(equipTable), BorderLayout.CENTER);
        
        JButton unequipBtn = new JButton("선택 장비 해제");
        unequipBtn.addActionListener(e -> handleUnequip());
        equipPanel.add(unequipBtn, BorderLayout.SOUTH);
        
        tabbedPane.addTab("착용 중인 장비", equipPanel);
        
        // 탭 2: 가방 (Inventory)
        JPanel invenPanel = new JPanel(new BorderLayout());
        String[] invenCols = {"아이템 이름", "부위", "효과", "가격"};
        inventoryModel = new DefaultTableModel(invenCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        inventoryTable = new JTable(inventoryModel);
        invenPanel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        
        JButton equipBtn = new JButton("선택 아이템 장착");
        equipBtn.addActionListener(e -> handleEquip());
        invenPanel.add(equipBtn, BorderLayout.SOUTH);
        
        tabbedPane.addTab("가방 (인벤토리)", invenPanel);
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // 3. 하단: 닫기 버튼
        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dispose());
        mainPanel.add(closeButton, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    // 데이터 새로고침 (화면 갱신)
    private void refreshData() {
        // 1. 착용 장비 갱신
        equipModel.setRowCount(0);
        Map<Item.EquipSlot, Item> equipped = player.getEquippedItems();
        for (Map.Entry<Item.EquipSlot, Item> entry : equipped.entrySet()) {
            Item item = entry.getValue();
            String effectStr = item.getEffectType().getDesc() + " +" + item.getEffectValue();
            equipModel.addRow(new Object[]{
                entry.getKey().getKoreanName(), // 부위 (한글)
                item.getName(),
                effectStr
            });
        }
        
        // 2. 인벤토리 갱신
        inventoryModel.setRowCount(0);
        for (Item item : player.getInventory()) {
            String effectStr = item.getEffectType().getDesc() + " +" + item.getEffectValue();
            inventoryModel.addRow(new Object[]{
                item.getName(),
                item.getSlot().getKoreanName(),
                effectStr,
                item.getPrice() + " G"
            });
        }
    }
    
    // [장착] 버튼 로직
    private void handleEquip() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow >= 0) {
            // 선택된 행의 아이템 가져오기 (List 순서와 Table 순서가 같다고 가정)
            Item item = player.getInventory().get(selectedRow);
            player.equipItem(item); // Player의 장착 로직 실행
            refreshData(); // 화면 갱신
            JOptionPane.showMessageDialog(this, "장착했습니다!");
        } else {
            JOptionPane.showMessageDialog(this, "장착할 아이템을 선택해주세요.");
        }
    }
    
    // [해제] 버튼 로직
    private void handleUnequip() {
        int selectedRow = equipTable.getSelectedRow();
        if (selectedRow >= 0) {
            // 테이블의 '부위' 컬럼(0번)을 보고 어떤 슬롯인지 찾음
            String slotName = (String) equipModel.getValueAt(selectedRow, 0);
            
            // 한글 이름으로 EquipSlot 찾기 (약간 비효율적이지만 간단한 방법)
            Item.EquipSlot targetSlot = null;
            for (Item.EquipSlot slot : Item.EquipSlot.values()) {
                if (slot.getKoreanName().equals(slotName)) {
                    targetSlot = slot;
                    break;
                }
            }
            
            if (targetSlot != null) {
                player.unequipItem(targetSlot); // Player의 해제 로직 실행
                refreshData(); // 화면 갱신
                JOptionPane.showMessageDialog(this, "장착을 해제했습니다.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "해제할 장비를 선택해주세요.");
        }
    }
}