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
        // 플레이어 기본 정보 표시
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
        // 테이블 모델 정의: 장착 장비 목록을 표시
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
        // 테이블 모델 정의: 인벤토리 목록을 표시
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
        // Player.getEquippedItems()는 Map<EquipSlot, Item>을 반환합니다.
        Map<Item.EquipSlot, Item> equipped = player.getEquippedItems();
        
        // (11/21) [수정] Map을 순회하기 위해 .entrySet()을 사용해야 합니다.
        // Map 객체를 바로 for-each 루프에 사용할 수 없다는 컴파일 오류를 해결합니다.
        for (Map.Entry<Item.EquipSlot, Item> entry : equipped.entrySet()) {
            Item item = entry.getValue();
            // null 슬롯은 스킵
            if (item == null) continue; 
            
            String effectStr = item.getEffectType().getDesc() + " +" + item.getEffectValue();
            equipModel.addRow(new Object[]{
                entry.getKey().getKoreanName(), // 부위 (한글)
                item.getName(),
                effectStr
            });
        }
        
        // 2. 인벤토리 갱신
        inventoryModel.setRowCount(0);
        
        // (11/21) [수정] Player.getInventory()가 Inventory 객체를 반환하도록 변경됨에 따라,
        // List<Item>을 얻기 위해 Inventory 객체의 .getItems()를 호출합니다.
        for (Item item : player.getInventory().getItems()) { 
            String effectStr = item.getEffectType().getDesc() + " +" + item.getEffectValue();
            
            // 인벤토리 테이블에는 장착 부위를 한글로 표시
            String slotName = item.getSlot().getKoreanName();
            if (item.getSlot() == Item.EquipSlot.NONE) {
                slotName = "소모품"; // 장착 슬롯이 없는 아이템은 '소모품' 등으로 표시
            }
            
            inventoryModel.addRow(new Object[]{
                item.getName(),
                slotName,
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
            // (11/21) [수정] Player.getInventory()가 Inventory 객체를 반환하므로,
            // List<Item>을 얻기 위해 .getItems().get(selectedRow)를 사용합니다.
            Item item = player.getInventory().getItems().get(selectedRow);
            
            player.equipItem(item); // Player의 장착 로직 실행 (Inventory에 위임됨)
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
                player.unequipItem(targetSlot); // Player의 해제 로직 실행 (Inventory에 위임됨)
                refreshData(); // 화면 갱신
                JOptionPane.showMessageDialog(this, "장착을 해제했습니다.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "해제할 장비를 선택해주세요.");
        }
    }
}