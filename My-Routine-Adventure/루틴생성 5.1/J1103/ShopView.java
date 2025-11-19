package J1103;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ShopView extends JFrame {

    private final Player player;
    private final MainDashboard dashboard; // 구매 후 메인화면 UI 갱신용
    private List<Item> shopItems;          // 상점에서 파는 물건 리스트
    private JLabel goldLabel;              // 실시간 골드 표시용

    public ShopView(Player player, MainDashboard dashboard) {
        this.player = player;
        this.dashboard = dashboard;

        setTitle("아이템 상점");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        // 1. 상점 재고 입고 (하드코딩된 데이터)
        initShopItems();

        // 2. UI 구성
        initUI();

        setVisible(true);
    }

    private void initShopItems() {
        shopItems = new ArrayList<>();

        // --- 무기 (보스 공격력 위주) ---
        shopItems.add(new Item("수련용 목검", 50, Item.EquipSlot.WEAPON, Item.EffectType.BOSS_ATTACK, 5));
        shopItems.add(new Item("무거운 철검", 200, Item.EquipSlot.WEAPON, Item.EffectType.BOSS_ATTACK, 15));
        shopItems.add(new Item("용사의 검", 1000, Item.EquipSlot.WEAPON, Item.EffectType.BOSS_ATTACK, 50));

        // --- 투구 (경험치 위주) ---
        shopItems.add(new Item("가죽 모자", 100, Item.EquipSlot.HEAD, Item.EffectType.EXP_BONUS, 5));
        shopItems.add(new Item("학자의 안경", 300, Item.EquipSlot.HEAD, Item.EffectType.EXP_BONUS, 10));

        // --- 갑옷 (밸런스) ---
        shopItems.add(new Item("여행자의 옷", 150, Item.EquipSlot.BODY, Item.EffectType.EXP_BONUS, 5));
        shopItems.add(new Item("기사의 갑옷", 500, Item.EquipSlot.BODY, Item.EffectType.BOSS_ATTACK, 20));

        // --- 장신구 (특수) ---
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. 상단: 환영 메시지 및 골드 표시
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        JLabel titleLabel = new JLabel("어서오세요! 없는 것 빼고 다 있는 상점입니다.", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        
        goldLabel = new JLabel("내 보유 골드: " + player.getGold() + " G", JLabel.CENTER);
        goldLabel.setForeground(new Color(200, 150, 0)); // 금색 느낌
        goldLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        topPanel.add(titleLabel);
        topPanel.add(goldLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 2. 중앙: 아이템 목록 테이블
        String[] headers = {"아이템 이름", "부위", "효과", "가격"};
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // 테이블에 데이터 채우기
        for (Item item : shopItems) {
            String effectStr = item.getEffectType().getDesc() + " +" + item.getEffectValue();
            model.addRow(new Object[]{
                item.getName(),
                item.getSlot().getKoreanName(),
                effectStr,
                item.getPrice() + " G"
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. 하단: 구매 버튼
        JButton buyButton = new JButton("선택한 아이템 구매하기");
        buyButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        buyButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                Item selectedItem = shopItems.get(selectedRow);
                handleBuy(selectedItem);
            } else {
                JOptionPane.showMessageDialog(this, "구매할 아이템을 선택해주세요.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(buyButton);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * 구매 로직 처리
     */
    private void handleBuy(Item item) {
        // 1. 정말 구매할지 확인
        int confirm = JOptionPane.showConfirmDialog(this, 
            "[" + item.getName() + "]을(를) " + item.getPrice() + "G에 구매하시겠습니까?",
            "구매 확인", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        // 2. 플레이어 구매 시도
        boolean success = player.buyItem(item);

        if (success) {
            // 성공 시 효과음이나 메시지
            JOptionPane.showMessageDialog(this, "구매 성공! 인벤토리를 확인하세요.");
            
            // UI 갱신 (상점 내 골드 표시)
            goldLabel.setText("내 보유 골드: " + player.getGold() + " G");
            
            // ⭐ 메인 화면(대시보드)의 골드 표시도 갱신
            if (dashboard != null) {
                dashboard.updatePlayerStatusUI();
            }
        } else {
            // 골드 부족
            JOptionPane.showMessageDialog(this, "골드가 부족합니다!", "구매 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
}