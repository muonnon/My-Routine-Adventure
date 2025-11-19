package J1103;

import java.io.Serializable; // ⭐ Serializable 인터페이스 추가 (파일 저장을 위해)
// 25.11.19 - 김민기 : 장착되지 않은 아이템에 대해 보관하는 아이템 '리스트' , 현재 착용중인 아이템 '맵' , 장착 및 해제 로직 구현, 보너스 수치계산 
import java.time.LocalDate;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
// -------------------
import java.util.Set;


public class Player implements Serializable { // ⭐ Serializable 인터페이스 구현
    private static final long serialVersionUID = 1L; // 직렬화 버전 UID
    
    private String name;
    private int level;
    private int currentExp;
    private int maxExp; // 현재 레벨에서 필요한 최대 경험치
    private int gold;
    // 25.11.19- 김민기 :  1. 인벤토리 (가방): 보유 중인 아이템 목록
    private List<Item> inventory;
    // 25.11.19- 김민기 :  2. 장착 중인 아이템: (슬롯, 아이템) 쌍으로 저장 (예: HEAD -> 철투구)
    private Map<Item.EquipSlot, Item> equippedItems;
    private Set<LocalDate> streakDates; // ⭐ 25.11.19 [추가] 루틴을 완료한 날짜 기록 (연속 달성 현황용) 
    
    
    
    // 생성자: 이름과 초기값을 설정합니다.
    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.currentExp = 0;
        // ⭐ Lv.1의 maxExp는 100으로 설정 (calculateMaxExp 메서드 사용)
        this.maxExp = calculateMaxExp(this.level); 
        this.gold = 0; 
        // 25.11.19 - 김민기 :  리스트와 맵 초기화
        this.inventory = new ArrayList<>();
        this.equippedItems = new HashMap<>();
        this.streakDates = new HashSet<>();  // 25.11.19 연속일자 체크용
    }
    
    // =========2025. 11. 19 - 김민기 : ==============================
    // ⭐ [신규 기능] 아이템 구매, 장착, 해제 로직
    // =============================================================

    /**
     * 아이템 구매: 골드를 차감하고 인벤토리에 추가
     */
    public boolean buyItem(Item item) {
        if (this.gold >= item.getPrice()) {
            this.gold -= item.getPrice();
            this.inventory.add(item); // 가방에 넣음
            return true;
        }
        return false;
    }
    
    /**
     * 아이템 장착: 인벤토리에서 빼서 장비 슬롯에 착용
     * (이미 해당 슬롯에 장비가 있다면 교체)
     */
    public void equipItem(Item item) {
        if (!inventory.contains(item)) return; // 없는 아이템은 장착 불가

        // 1. 인벤토리에서 제거
        inventory.remove(item);

        // 2. 해당 슬롯에 이미 착용 중인 아이템이 있는지 확인
        Item.EquipSlot slot = item.getSlot();
        if (equippedItems.containsKey(slot)) {
            // 이미 착용 중인 게 있다면 벗어서 인벤토리로 돌려보냄 (스위칭)
            Item oldItem = equippedItems.get(slot);
            inventory.add(oldItem);
        }
        
        // 새 장비를 장착 목록에 추가하는 코드
        equippedItems.put(slot, item);
        System.out.println(item.getName() + " 장착 완료!");
    }
    
    /**
     * 아이템 장착 해제: 장비 슬롯에서 빼서 인벤토리로 이동
     */
    public void unequipItem(Item.EquipSlot slot) {
        if (equippedItems.containsKey(slot)) {
            Item item = equippedItems.remove(slot); // 장비창에서 제거
            inventory.add(item); // 인벤토리로 이동
            System.out.println(item.getName() + "을(를) 장착 해제했습니다.");
        }
    }

    // =========2025. 11. 19 - 김민기 ================================
    // ⭐ [신규 기능] 능력치 계산 (착용 중인 장비만 계산)
    // =============================================================

    /**
     * 현재 착용 중인 모든 장비의 '보스 공격력' 합산
     */
    public int getTotalBonusDamage() {
        int total = 0;
        for (Item item : equippedItems.values()) {
            if (item.getEffectType() == Item.EffectType.BOSS_ATTACK) {
                total += item.getEffectValue();
            }
        }
        return total;
    }
    
    /**
     * 현재 착용 중인 모든 장비의 '추가 경험치' 합산
     */
    public int getTotalBonusExp() {
        int total = 0;
        for (Item item : equippedItems.values()) {
            if (item.getEffectType() == Item.EffectType.EXP_BONUS) {
                total += item.getEffectValue();
            }
        }
        return total;
    }
        
    
    // =========================================================================
    // 레벨업 로직 관련 메서드
    // =========================================================================

    /**
     * 특정 레벨에서 다음 레벨로 가기 위해 필요한 총 경험치(maxExp)를 계산합니다.
     * Lv.1 -> 100, Lv.2 -> 150, Lv.3 -> 200, ... (레벨당 50 증가)
     */
    private int calculateMaxExp(int level) {
        if (level < 1) return 100;
        // 공식: 100 + 50 * (level - 1)
        return 100 + 50 * (level - 1);
    }
    
    
    /**
     * 경험치를 획득하고 레벨업 여부를 확인하여 처리합니다.
     * 이 메서드가 RoutineManager에서 호출되어야 합니다.
     */
    public void gainExp(int exp) {
    	//    	여기서 보너스 경험치 적용은 하지 않음 (RoutineManager에서 호출할 때 계산해서 넘겨줌
    	//    	일단은 순수하게 들어온 양만 증가시키는 것으로 유지
    	// 1. 경험치 추가
        this.currentExp += exp;
        
        // 2. 레벨업 반복 확인 및 처리
        while (this.currentExp >= this.maxExp) {
            
            // 2-1. 초과 경험치 계산 (다음 레벨로 이월될 양)
            int remainingExp = this.currentExp - this.maxExp;
            
            // 2-2. 레벨업
            this.level++;
            
            // 2-3. maxExp 및 currentExp 업데이트
            this.maxExp = calculateMaxExp(this.level); // 다음 레벨의 필요 경험치 설정
            this.currentExp = remainingExp; // 초과 경험치 이월
            
            // TODO: MainDashboard에 레벨업 알림 로그를 추가하거나, 
            // RoutineManager를 통해 로그를 출력할 수 있도록 설계해야 합니다.
            System.out.println("🎉 레벨업! 현재 레벨: Lv." + this.level + 
                               " (다음 레벨 필요 EXP: " + this.maxExp + ")");
        }
    }


    // =========================================================================
    // Getters / Setters
    // =========================================================================
    
    // Getters (UI에 값을 표시하기 위해 사용)
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getCurrentExp() { return currentExp; }
    public int getMaxExp() { return maxExp; }
    public int getGold() { return gold; }
    // 2025.11.19 - 김민기 : 인벤토리 UI에서 사용하기 위해 추가해야 하는 Getters
    public List<Item> getInventory() { return inventory; }
    public Map<Item.EquipSlot, Item> getEquippedItems() { return equippedItems; }
    public Set<LocalDate> getStreakDates() { return streakDates; } // 25.11.19 연속일자용
    
    // Setters (주로 파일 로드 시 사용)
    public void setName(String name) { this.name = name; }
    public void setLevel(int level) { 
        this.level = level; 
        this.maxExp = calculateMaxExp(level); // 레벨 설정 시 maxExp도 재계산
    }
    public void setCurrentExp(int currentExp) { 
        // 이 Setter는 주로 파일 로드 시에만 사용하도록 남겨둡니다.
        this.currentExp = currentExp; 
    }
    public void setMaxExp(int maxExp) { 
        // 이 Setter는 외부에서 직접 maxExp를 바꾸는 것을 막기 위해 제거하거나 private으로 만드는 것이 좋습니다. 
        // 여기서는 파일 로드 호환성을 위해 유지하되, 내부 로직에 의해 변경되도록 합니다.
        this.maxExp = maxExp;
    }
    public void setGold(int gold) { 
        this.gold = gold; 
    }

    /**
     * 골드를 획득합니다.
     */
    public void gainGold(int gold) {
        this.gold += gold;
    }
}